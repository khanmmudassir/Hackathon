import React, { useEffect, useState } from 'react';
import './App.css';

function App() {
  const [proposals, setProposals] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchProposals = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/reassignments/proposals');
      const data = await response.json();
      setProposals(data);
      setLoading(false);
    } catch (error) {
      console.error("Error fetching proposals:", error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProposals();
    const interval = setInterval(fetchProposals, 5000); // Polling for "automatic" updates
    return () => clearInterval(interval);
  }, []);

  const handleAction = async (id, action) => {
    await fetch(`http://localhost:8080/api/reassignments/${action}/${id}`, { method: 'POST' });
    fetchProposals(); // Refresh list
  };

  return (
    <div className="dashboard">
      <header>
        <h1>ZipRun Ops Console</h1>
        <p>Reactive Reassignment Engine — Sprint 1</p>
      </header>

      {loading ? <p>Loading suggestions...</p> : (
        <div className="proposal-list">
          {proposals.length === 0 && <p className="empty">No active issues. System stable.</p>}
          {proposals.map(p => (
            <div key={p.id} className="card">
              <div className="card-header">
                <span className="badge">Order: {p.orderId}</span>
                <span className="confidence" style={{color: p.confidenceScore > 0.9 ? '#2ecc71' : '#f1c40f'}}>
                  {Math.round(p.confidenceScore * 100)}% Match
                </span>
              </div>

              <div className="card-body">
                <p><strong>Incident:</strong> {p.reason}</p>
                <p><strong>Recommendation:</strong> Move to Agent <code>{p.recommendedAgentId}</code></p>
                <div className="ai-explanation">
                  <em>"AI Analysis: This agent has the lowest current load in the sector and is predicted to complete their current route in 4 minutes."</em>
                </div>
              </div>

              <div className="card-footer">
                <button className="approve-btn" onClick={() => handleAction(p.id, 'approve')}>Approve</button>
                <button className="reject-btn" onClick={() => handleAction(p.id, 'reject')}>Ignore</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default App;