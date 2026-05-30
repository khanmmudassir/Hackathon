import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

/**
 * MOCK_DATA: High-fidelity samples for the Ops Console.
 * These simulate the AI's output from the Gemini T-4 loop.
 */
const MOCK_PROPOSALS = [
  {
    id: "prop-101",
    orderId: "ORD-7721",
    recommendedAgentId: "DRIVER-ALPHA",
    confidenceScore: 0.94,
    reasoning: "Original agent reported a flat tire. DRIVER-ALPHA is 0.8km away and has no active deliveries, minimizing SLA breach risk by 12 minutes.",
    triggerReason: "AGENT_OFFLINE",
    status: "PENDING",
    createdAt: new Date().toISOString()
  },
  {
    id: "prop-102",
    orderId: "ORD-8840",
    recommendedAgentId: "DRIVER-ZETA",
    confidenceScore: 0.68,
    reasoning: "High traffic detected on current route. DRIVER-ZETA is further away but on a clear path. Confidence lower due to narrow delivery window.",
    triggerReason: "INITIAL",
    status: "PENDING",
    createdAt: new Date().toISOString()
  }
];

// Passing mock data as a prop allows you to test the UI
// even before your fetch() logic is fully wired to the backend.
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App initialData={MOCK_PROPOSALS} />
  </React.StrictMode>
);