import axios from "axios";

const API_BASE = "/api/workorders"; // relative path

export const fetchWorkOrders = async () => {
  const res = await axios.get(API_BASE);
  return res.data;
};

export const createWorkOrder = async (workOrder) => {
  const res = await axios.post(API_BASE, workOrder);
  return res.data;
};

export const updateWorkOrderStatus = async (id, status) => {
  const res = await axios.patch(`${API_BASE}/${id}/status`, { status });
  return res.data;
};
