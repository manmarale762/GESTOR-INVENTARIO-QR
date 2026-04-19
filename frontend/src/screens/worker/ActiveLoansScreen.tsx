import React, { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { EmptyState } from '../../components/EmptyState';
import { LoanCard } from '../../components/LoanCard';
import { Screen } from '../../components/Screen';
import { SectionHeader } from '../../components/SectionHeader';
import { api } from '../../services/api';
import { Loan } from '../../types';

export function ActiveLoansScreen() {
  const { session } = useAuth();
  const [loans, setLoans] = useState<Loan[]>([]);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    if (!session) return;
    setRefreshing(true);
    const next = await api.getLoans(session.user.id);
    setLoans(next);
    setRefreshing(false);
  }, [session]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <Screen refreshing={refreshing} onRefresh={load}>
      <SectionHeader title="Préstamos activos" description="Consulta los activos de alto valor que están bajo tu responsabilidad." />
      {loans.length ? loans.map((loan) => <LoanCard key={loan.id} loan={loan} />) : <EmptyState title="Sin préstamos" message="No tienes materiales asignados en este momento." />}
    </Screen>
  );
}
