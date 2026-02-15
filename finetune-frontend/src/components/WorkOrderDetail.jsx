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
      <div className="mt-6 flex gap-4">
        <Link
          to={`/workorders/${id}/edit`}
          className="bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600"
        >
          Edit
        </Link>
        <Link
          to="/workorders"
          className="bg-gray-300 px-4 py-2 rounded hover:bg-gray-400"
        >
          Back to Dashboard
        </Link>
      </div>
    </div>
  );
};

export default WorkOrderDetail;
