import React, { useState, useEffect } from 'react';

const API_BASE = "/api";

function App() {
  const [proposals, setProposals] = useState([]);
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [connectionError, setConnectionError] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [propRes, orderRes] = await Promise.all([
          fetch(`${API_BASE}/reassignments/proposals`),
          fetch(`${API_BASE}/orders`)
        ]);

        if (!propRes.ok || !orderRes.ok) throw new Error("Backend unreachable");

        const propData = await propRes.json();
        const orderData = await orderRes.json();

        setProposals(propData);
        setOrders(orderData);
        setConnectionError(false);
      } catch (err) {
        console.error("Polling error:", err);
        setConnectionError(true);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
    const poll = setInterval(fetchData, 3000);
    return () => clearInterval(poll);
  }, []);

  const handleAction = async (id, status) => {
    // Optimistic UI update
    setProposals(prev => prev.filter(p => p.id !== id));

    try {
      const response = await fetch(`${API_BASE}/suggestions/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
      });
      if (!response.ok) throw new Error("Update failed");
    } catch (err) {
      console.error("Action error:", err);
      // Refresh to sync state if call fails
    }
  };

  return (
    <div className="p-8 text-white min-h-screen bg-gray-900">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-blue-400">Ops Console</h1>
        {connectionError && (
          <span className="text-red-400 animate-pulse text-sm">Offline: Check Backend (Port 8080)</span>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* AI Proposals Section */}
        <div className="bg-gray-800 p-6 rounded-xl shadow-lg border border-gray-700">
          <h2 className="text-xl font-semibold mb-4 border-b border-gray-700 pb-2 flex justify-between">
            AI Proposals
            <span className="text-xs bg-blue-900 text-blue-200 px-2 py-1 rounded-full">{proposals.length}</span>
          </h2>

          {proposals.length === 0 ? (
            <div className="text-gray-500 italic py-10 text-center">
              {isLoading ? "Analyzing logistical gaps..." : "No pending reassignments."}
            </div>
          ) : (
            proposals.map(p => (
              <div key={p.id} className="bg-gray-900 p-4 rounded-lg mb-4 border-l-4 border-blue-500 transition-all hover:bg-gray-950">
                <div className="flex justify-between items-start">
                  <p className="font-bold text-lg">Order: {p.orderId}</p>
                  <span className="text-xs bg-blue-900/50 text-blue-300 px-2 py-0.5 rounded">
                    {Math.round((p.confidenceScore || 0) * 100)}% Match
                  </span>
                </div>
                <p className="text-sm text-gray-400 mt-1 italic">"{p.reason || 'Optimizing efficiency'}"</p>
                <div className="mt-3 p-2 bg-gray-800 rounded border border-gray-700">
                  <p className="text-sm text-green-400">Recommend: <span className="font-mono">{p.recommendedAgentId}</span></p>
                </div>
                <div className="flex gap-3 mt-4">
                  <button
                    onClick={() => handleAction(p.id, 'ACCEPTED')}
                    className="flex-1 bg-blue-600 hover:bg-blue-500 py-2 rounded font-medium transition-colors"
                  >
                    Approve
                  </button>
                  <button
                    onClick={() => handleAction(p.id, 'REJECTED')}
                    className="flex-1 bg-gray-700 hover:bg-gray-600 py-2 rounded font-medium transition-colors"
                  >
                    Ignore
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Active Orders Section */}
        <div className="bg-gray-800 p-6 rounded-xl shadow-lg border border-gray-700">
          <div className="flex justify-between items-center mb-4 border-b border-gray-700 pb-2">
            <h2 className="text-xl font-semibold">Active Orders</h2>
            <span className="text-xs text-gray-400">{orders.length} Total Shipments</span>
          </div>

          {orders.length === 0 ? (
            <div className="text-gray-500 italic py-10 text-center">No active orders in system.</div>
          ) : (
            <div className="space-y-3 overflow-y-auto max-h-[600px] pr-2 custom-scrollbar">
              {orders.map(o => {
                const agentLoad = orders.filter(ord => ord.assignedAgent?.id === o.assignedAgent?.id).length;

                return (
                  <div key={o.id} className="bg-gray-900/50 p-3 rounded-lg border border-gray-700/50 flex justify-between items-center hover:border-blue-500/50 transition-colors">
                    <div className="flex flex-col">
                      <span className="font-mono text-blue-300 font-bold">{o.id}</span>
                      <span className="text-[10px] text-gray-500 uppercase tracking-wider">Express Delivery</span>
                    </div>

                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p className="text-sm font-medium text-gray-200">
                          {o.assignedAgent ? o.assignedAgent.name : 'Unassigned'}
                        </p>

                        {/* Added Agent ID Display */}
                        {o.assignedAgent && (
                          <p className="text-[10px] font-mono text-blue-500/70 uppercase">
                            {o.assignedAgent.id}
                          </p>
                        )}
                      </div>
                      <span className={`w-2.5 h-2.5 rounded-full ${o.status === 'ASSIGNED' ? 'bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.6)]' : 'bg-yellow-500 animate-pulse'}`}></span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;