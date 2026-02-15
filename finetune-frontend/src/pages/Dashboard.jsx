// src/pages/Dashboard.jsx
import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { fetchWorkOrders, deleteWorkOrder } from '../services/api';
import WorkOrderCard from '../components/WorkOrderCard';
import { Link } from 'react-router-dom';

const Dashboard = () => {
  const location = useLocation();
  const [workOrders, setWorkOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Check for success message from navigation
  useEffect(() => {
    if (location.state?.message) {
      setSuccessMessage(location.state.message);
      // Clear message after 5 seconds
      setTimeout(() => setSuccessMessage(null), 5000);
    }
  }, [location]);

  const fetchData = async () => {
    try {
      const data = await fetchWorkOrders();
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
        setSuccessMessage('Work order deleted successfully');
        setTimeout(() => setSuccessMessage(null), 3000);
      } catch (err) {
        alert('Failed to delete work order.');
      }
    }
  };

  useEffect(() => {
    fetchData();
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

      {/* Success Message */}
      {successMessage && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-6">
          <div className="flex justify-between items-center">
            <span>{successMessage}</span>
            <button 
              onClick={() => setSuccessMessage(null)}
              className="text-green-700 hover:text-green-900 font-bold"
            >
              ✕
            </button>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {workOrders.map((wo) => (
          <WorkOrderCard key={wo.id} workOrder={wo} onDelete={handleDelete} />
        ))}
      </div>
    </div>
  );
};

export default Dashboard;
