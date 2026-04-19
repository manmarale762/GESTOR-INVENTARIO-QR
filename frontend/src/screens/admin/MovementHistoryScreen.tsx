import React, { useCallback, useEffect, useState } from 'react';
import { HistoryCard } from '../../components/HistoryCard';
import { Screen } from '../../components/Screen';
import { SectionHeader } from '../../components/SectionHeader';
import { api } from '../../services/api';
import { ScanRecord } from '../../types';

export function MovementHistoryScreen() {
  const [records, setRecords] = useState<ScanRecord[]>([]);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    setRefreshing(true);
    setRecords(await api.getHistory());
    setRefreshing(false);
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <Screen refreshing={refreshing} onRefresh={load}>
      <SectionHeader title="Historial de movimientos" description="Registro de accesos, retiradas y devoluciones validadas." />
      {records.map((record) => <HistoryCard key={record.id} record={record} />)}
    </Screen>
  );
}
