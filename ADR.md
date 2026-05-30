**ADR 001: Autonomous Agentic Reassignment Loop**

**Date**
30-05-2026

**Context**
In high-frequency logistics environments, the sudden unavailability of delivery agents (due to accidents, vehicle failure, or personal emergencies) leads to delivery delays and operational bottlenecks. Manual reassignment by human dispatchers is slow and does not scale. We needed a system that could:
1.Detect agent failures in real-time.
2.Automatically propose the "next best" agent using intelligent routing.
3.Allow human-in-the-loop (HITL) approval while maintaining system autonomy.

**Decision**
We implemented an Event-Driven Agentic Loop using Spring Boot and React. Key components include:
1. Intelligent Routing EngineWe decoupled decision logic into a RoutingEngine that supports multiple strategies.
    •AI Strategy: Leverages the Gemini 1.5 Flash API to analyze agent load, proximity, and historical performance to provide a RoutingResult with a confidence score and natural language reasoning.
    •Rule-Based Fallback: A deterministic strategy that selects agents based on the lowest current active order count.
2. State Management & SerializationTo solve the "Infinite Recursion" issue common in JPA Many-to-Many relationships, we moved away from @JsonBackReference in favor of @JsonIgnoreProperties("orders"). This allows the frontend to view agent details (ID, Name, Load) within an Order object without triggering stack overflow errors during JSON serialization.
3. Resilience & Circuit BreakingThe ReassignmentEngine checks the health of the Gemini API (isAvailable()). If the AI service is unreachable (e.g., API versioning 404s or rate limits), the system automatically flips the strategy to ruleBased to ensure deliveries are not stalled.
4. Human-In-The-Loop (HITL) WorkflowWe introduced a ReassignmentProposal entity. Instead of the AI changing the database silently, it creates a proposal.•Approve: Triggers the state change (Order reassignment).•Reject: Feeds back into the loop, marking the suggested agent as "excluded" and requesting a new recommendation from the AI.
5. Security & Cross-Origin Resource Sharing (CORS)To facilitate rapid development in a hackathon environment, CSRF protection was disabled for stateless API usage, and a global CORS configuration was applied to allow the Vite frontend (port 5173) to communicate with the Spring Boot backend (port 8080).

**Consequences**
**Positive**
•Reduced Latency: Orders are flagged for reassignment the millisecond an agent goes offline.
•Transparency: Operators see why the AI made a choice via the reasoning field in the UI.
•Scalability: New routing strategies (e.g., GPS-based, weather-aware) can be added without changing the core engine.

**Negative**
•Polling Overhead: Current frontend implementation uses 3-second polling. For massive scale, this should migrate to WebSockets or Server-Sent Events (SSE).
•Consistency: "Optimistic UI" updates on the frontend could lead to temporary desync if the backend PATCH request fails.

**Code Structure Summary**
| Component | Responsibility | ReassignmentLoop | Listens for AgentOfflineEvent and triggers the engine | ReassignmentEngine | Orchestrates the workflow: checks AI health, saves proposals, executes reassignments | RoutingEngine | The "Brain." Switches between Gemini AI and hardcoded logic | App.jsx | The "Ops Console." Visualizes active load, agent IDs, and AI proposals. |