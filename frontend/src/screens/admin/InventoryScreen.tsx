import React, { useCallback, useEffect, useState } from 'react';
import { InventoryCard } from '../../components/InventoryCard';
import { Screen } from '../../components/Screen';
import { SectionHeader } from '../../components/SectionHeader';
import { api } from '../../services/api';
import { InventoryItem } from '../../types';

export function InventoryScreen() {
  const [items, setItems] = useState<InventoryItem[]>([]);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    setRefreshing(true);
    setItems(await api.getInventory());
    setRefreshing(false);
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <Screen refreshing={refreshing} onRefresh={load}>
      <SectionHeader title="Inventario corporativo" description="Vista operativa del material de alto valor y su estado actual." />
      {items.map((item) => <InventoryCard key={item.id} item={item} />)}
    </Screen>
  );
}
