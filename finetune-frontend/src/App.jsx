// src/App.jsx
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import WorkOrderDetail from './components/WorkOrderDetail';
import WorkOrderWizard from './components/WorkOrderWizard';

const App = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/workorders" />} />
        <Route path="/workorders" element={<Dashboard />} />
        <Route path="/workorders/new" element={<WorkOrderWizard />} />
        <Route path="/workorders/:id" element={<WorkOrderDetail />} />
      </Routes>
    </Router>
  );
};

export default App;
