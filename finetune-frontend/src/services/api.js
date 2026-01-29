// src/api.js
import axios from 'axios';

const API_BASE = 'http://localhost:8080'; // Change if your Spring Boot server is different

export const getWorkOrders = async () => {
  try {
    const response = await axios.get(`${API_BASE}/workorders`);
    return response.data;
  } catch (error) {
    console.error('Error fetching work orders:', error);
    throw error;
  }
};

export const getWorkOrderById = async (id) => {
  try {
    const response = await axios.get(`${API_BASE}/workorders/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching work order ${id}:`, error);
    throw error;
  }
};

export const createWorkOrder = async (workOrder) => {
  try {
    const response = await axios.post(`${API_BASE}/workorders`, workOrder);
    return response.data;
  } catch (error) {
    console.error('Error creating work order:', error);
    throw error;
  }
};

export const updateWorkOrder = async (id, workOrder) => {
  try {
    const response = await axios.put(`${API_BASE}/workorders/${id}`, workOrder);
    return response.data;
  } catch (error) {
    console.error(`Error updating work order ${id}:`, error);
    throw error;
  }
};

export const deleteWorkOrder = async (id) => {
  try {
    await axios.delete(`${API_BASE}/workorders/${id}`);
  } catch (error) {
    console.error(`Error deleting work order ${id}:`, error);
    throw error;
  }
};
