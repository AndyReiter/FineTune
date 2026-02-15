import React, { useEffect, useState } from 'react';
import { workOrderApi } from '../api/apiClient';

export default function Dashboard() {
  const [stats, setStats] = useState({ total: 0, received: 0, inProgress: 0, readyForPickup: 0, completed: 0 });
  const [recentWorkOrders, setRecentWorkOrders] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const workOrders = await workOrderApi.getAll();
      
      const countByStatus = {
        RECEIVED: 0,
        IN_PROGRESS: 0,
        READY_FOR_PICKUP: 0,
        COMPLETED: 0,
      };

      workOrders.forEach(wo => {
        countByStatus[wo.status]++;
      });

      setStats({
        total: workOrders.length,
        received: countByStatus.RECEIVED,
        inProgress: countByStatus.IN_PROGRESS,
        readyForPickup: countByStatus.READY_FOR_PICKUP,
        completed: countByStatus.COMPLETED,
      });
      
      // Sort work orders by promisedBy (due date) first, then by createdAt
      const sortedWorkOrders = workOrders.sort((a, b) => {
        // Handle null promisedBy values - put them at the end
        if (!a.promisedBy && !b.promisedBy) {
          return new Date(a.createdAt) - new Date(b.createdAt);
        }
        if (!a.promisedBy) return 1; // a goes after b
        if (!b.promisedBy) return -1; // a goes before b
        
        // Both have promisedBy dates, sort by them
        const promisedDiff = new Date(a.promisedBy) - new Date(b.promisedBy);
        if (promisedDiff !== 0) return promisedDiff;
        
        // Same promisedBy date, sort by createdAt
        return new Date(a.createdAt) - new Date(b.createdAt);
      });
      
      setRecentWorkOrders(sortedWorkOrders.slice(0, 10));
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch(status) {
      case 'RECEIVED':
        return 'bg-blue-100 text-blue-800';
      case 'IN_PROGRESS':
        return 'bg-yellow-100 text-yellow-800';
      case 'READY_FOR_PICKUP':
        return 'bg-purple-100 text-purple-800';
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold mb-8">Dashboard</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-8">
        <div className="bg-white p-6 rounded shadow">
          <h3 className="text-gray-600 text-sm font-semibold mb-2">Total Work Orders</h3>
          <p className="text-4xl font-bold text-gray-900">{stats.total}</p>
        </div>
        <div className="bg-white p-6 rounded shadow">
          <h3 className="text-gray-600 text-sm font-semibold mb-2">Received</h3>
          <p className="text-4xl font-bold text-blue-600">{stats.received}</p>
        </div>
        <div className="bg-white p-6 rounded shadow">
          <h3 className="text-gray-600 text-sm font-semibold mb-2">In Progress</h3>
          <p className="text-4xl font-bold text-yellow-600">{stats.inProgress}</p>
        </div>
        <div className="bg-white p-6 rounded shadow">
          <h3 className="text-gray-600 text-sm font-semibold mb-2">Ready for Pickup</h3>
          <p className="text-4xl font-bold text-purple-600">{stats.readyForPickup}</p>
        </div>
        <div className="bg-white p-6 rounded shadow">
          <h3 className="text-gray-600 text-sm font-semibold mb-2">Completed</h3>
          <p className="text-4xl font-bold text-green-600">{stats.completed}</p>
        </div>
      </div>

      <div className="bg-white rounded shadow p-6">
        <h2 className="text-2xl font-bold mb-4">Recent Work Orders</h2>
        {loading ? (
          <p className="text-center text-gray-600">Loading...</p>
        ) : recentWorkOrders.length === 0 ? (
          <p className="text-gray-500">No work orders yet</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="text-left py-3 px-4 font-semibold">Customer</th>
                  <th className="text-left py-3 px-4 font-semibold">Phone</th>
                  <th className="text-left py-3 px-4 font-semibold">Email</th>
                  <th className="text-left py-3 px-4 font-semibold">Status</th>
                  <th className="text-left py-3 px-4 font-semibold">Due By</th>
                  <th className="text-left py-3 px-4 font-semibold">Created</th>
                </tr>
              </thead>
              <tbody>
                {recentWorkOrders.map(workOrder => (
                  <tr key={workOrder.id} className="border-b hover:bg-gray-50">
                    <td className="py-3 px-4 font-semibold">{workOrder.customerName}</td>
                    <td className="py-3 px-4">
                      <a href={`tel:${workOrder.phone}`} className="text-blue-600 hover:underline">
                        {workOrder.phone}
                      </a>
                    </td>
                    <td className="py-3 px-4">
                      <a href={`mailto:${workOrder.email}`} className="text-blue-600 hover:underline">
                        {workOrder.email}
                      </a>
                    </td>
                    <td className="py-3 px-4">
                      <span className={`inline-block px-3 py-1 rounded text-sm font-semibold ${getStatusColor(workOrder.status)}`}>
                        {workOrder.status.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="py-3 px-4" style={{color: workOrder.promisedBy ? '#059669' : '#6b7280', fontWeight: workOrder.promisedBy ? '600' : 'normal'}}>
                      {workOrder.promisedBy ? new Date(workOrder.promisedBy).toLocaleDateString() : 'Not Set'}
                    </td>
                    <td className="py-3 px-4 text-gray-600">
                      {workOrder.createdAt ? new Date(workOrder.createdAt).toLocaleDateString() : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
