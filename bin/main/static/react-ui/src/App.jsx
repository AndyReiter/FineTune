import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import WorkOrdersPage from './pages/WorkOrdersPage';
import './App.css';

export default function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <nav className="bg-white shadow">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between h-16">
              <div className="flex items-center">
                <h1 className="text-2xl font-bold text-gray-900">FineTune</h1>
              </div>
              <div className="flex gap-8 items-center">
                <Link to="/" className="text-gray-700 hover:text-gray-900 font-medium">Dashboard</Link>
                <Link to="/workorders" className="text-gray-700 hover:text-gray-900 font-medium">Work Orders</Link>
              </div>
            </div>
          </div>
        </nav>
        
        <main className="max-w-7xl mx-auto">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/workorders" element={<WorkOrdersPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}
