// src/components/WorkOrderCard.jsx
import React from 'react';
import { Link } from 'react-router-dom';

const WorkOrderCard = ({ workOrder, onDelete }) => {
  const { id, title, status, assignedTo, createdDate } = workOrder;

  return (
    <div className="border rounded-lg p-4 shadow hover:shadow-lg transition">
      <h2 className="text-xl font-semibold mb-2">{title}</h2>
      <p><strong>Status:</strong> {status}</p>
      <p><strong>Assigned To:</strong> {assignedTo}</p>
      <p><strong>Created:</strong> {new Date(createdDate).toLocaleDateString()}</p>
      {/* Show boot summary if this work order contains mount items with boots */}
      {workOrder.equipment && workOrder.equipment.some(e => e.serviceType === 'MOUNT' && e.boot) && (
        <div className="mt-2">
          <strong>Boots:</strong>
          <div className="text-sm text-indigo-700 italic">
            {workOrder.equipment.filter(e => e.serviceType === 'MOUNT' && e.boot)
              .map(e => `${e.boot.brand || ''} ${e.boot.model || ''}${e.boot.bsl ? ` (${e.boot.bsl}mm)` : ''}`)
              .filter(Boolean)
              .join(', ')}
          </div>
        </div>
      )}
      <div className="flex justify-between mt-4">
        <Link
          to={`/workorders/${id}`}
          className="text-blue-600 hover:underline"
        >
          View
        </Link>
        <button
          onClick={() => onDelete(id)}
          className="text-red-600 hover:underline"
        >
          Delete
        </button>
      </div>
    </div>
  );
};

export default WorkOrderCard;
