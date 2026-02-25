// src/components/WorkOrderDetail.jsx
import React, { useEffect, useState } from 'react';
import { fetchWorkOrder } from '../services/api';
import { useParams, Link } from 'react-router-dom';

const WorkOrderDetail = () => {
  const { id } = useParams();
  const [workOrder, setWorkOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadWorkOrder = async () => {
      try {
        const data = await fetchWorkOrder(id);
        setWorkOrder(data);
      } catch (err) {
        setError('Failed to load work order.');
      } finally {
        setLoading(false);
      }
    };
    loadWorkOrder();
  }, [id]);

  if (loading) return <div className="text-center mt-10">Loading...</div>;
  if (error) return <div className="text-center mt-10 text-red-500">{error}</div>;

  return (
    <div className="p-6 max-w-xl mx-auto">
      <h1 className="text-3xl font-bold mb-4">{workOrder.title}</h1>
      <p><strong>Status:</strong> {workOrder.status}</p>
      <p><strong>Assigned To:</strong> {workOrder.assignedTo}</p>
      <p><strong>Created:</strong> {new Date(workOrder.createdDate).toLocaleDateString()}</p>
      <p className="mt-4"><strong>ID:</strong> {workOrder.id}</p>
      {/* Manage buttons and boot summary */}
      <div className="mt-6 flex items-center gap-4">
        <Link
          to={`/workorders/${id}/edit`}
          className="bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600"
        >
          Manage Service
        </Link>
        {/* Inline boot summary for quick glance */}
        {workOrder.equipment && workOrder.equipment.some(e => e.serviceType === 'MOUNT' && e.boot) && (
          <div className="text-sm text-indigo-700 italic">
            Boots: {workOrder.equipment.filter(e => e.serviceType === 'MOUNT' && e.boot)
              .map(e => `${e.boot.brand || ''} ${e.boot.model || ''}${e.boot.bsl ? ` (${e.boot.bsl}mm)` : ''}`)
              .filter(Boolean)
              .join(', ')}
          </div>
        )}
        <Link
          to="/workorders"
          className="bg-gray-300 px-4 py-2 rounded hover:bg-gray-400"
        >
          Back to Dashboard
        </Link>
      </div>

      {/* Equipment list with boot details styled differently */}
      <div className="mt-6">
        <h2 className="text-xl font-semibold mb-2">Equipment</h2>
        {workOrder.equipment && workOrder.equipment.length > 0 ? (
          <ul className="space-y-4">
            {workOrder.equipment.map(item => (
              <li key={item.id} className="border rounded p-3">
                <div className="flex justify-between">
                  <div>
                    <div className="font-medium">{item.brand} {item.model}</div>
                    <div className="text-sm text-gray-600">Service: {item.serviceType} • Status: {item.status}</div>
                  </div>
                </div>
                {item.boot && (
                  <div className="mt-2 text-sm italic text-indigo-700">
                    <div><strong>Boot:</strong> {item.boot.brand} {item.boot.model} {item.boot.bsl ? `(${item.boot.bsl}mm)` : ''}</div>
                    <div className="text-xs text-indigo-600">Height: {item.boot.heightInches || '—'} in • Weight: {item.boot.weight || '—'} lbs • Age: {item.boot.age || '—'}</div>
                  </div>
                )}
              </li>
            ))}
          </ul>
        ) : (
          <div className="text-gray-600">No equipment listed for this work order.</div>
        )}
      </div>
    </div>
  );
};

export default WorkOrderDetail;
