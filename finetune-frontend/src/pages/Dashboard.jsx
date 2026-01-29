// src/components/Dashboard.jsx
import React, { useEffect, useState } from 'react';
import { getWorkOrders, deleteWorkOrder } from '../services/api';
import WorkOrderCard from '../components/WorkOrderCard';
import { Link } from 'react-router-dom';

const Dashboard = () => {
  const [workOrders, setWorkOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchWorkOrders = async () => {
    try {
      const data = await getWorkOrders();
      setWorkOrders(data);
    } catch (err) {
      setError('Failed to load work orders.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this work order?')) {
      try {
        await deleteWorkOrder(id);
        setWorkOrders(workOrders.filter((wo) => wo.id !== id));
      } catch (err) {
        alert('Failed to delete work order.');
      }
    }
  };

  useEffect(() => {
    fetchWorkOrders();
  }, []);

  if (loading) return <div className="text-center mt-10">Loading work orders...</div>;
  if (error) return <div className="text-center mt-10 text-red-500">{error}</div>;

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Work Orders</h1>
        <Link
          to="/workorders/new"
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          + New Work Order
        </Link>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {workOrders.map((wo) => (
          <WorkOrderCard key={wo.id} workOrder={wo} onDelete={handleDelete} />
        ))}
      </div>
    </div>
  );
};

export default Dashboard;
