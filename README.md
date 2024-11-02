# README: Obstruction-Free Consensus and Paxos Project

## Project Overview
This project, completed as part of the SLR210 course, focuses on designing and implementing an **obstruction-free consensus (OFC) algorithm** to support a fault-tolerant distributed system. By building on a consensus abstraction and leveraging the **AKKA actor-based programming model**, the project simulates a state-machine replicated system with robust fault tolerance properties.

## Project Objectives
The goal of this project is to:
1. Implement an OFC algorithm for asynchronous processes that can communicate through reliable point-to-point channels.
2. Simulate environments where up to `f < N/2` processes may crash, ensuring:
   - **Validity**: All decided values are proposed values.
   - **Agreement**: No two processes decide on different values.
   - **Obstruction-free termination**: Guarantees decision or abort under specified conditions.

## Environment and Setup
- **Programming Language**: Java (using AKKA framework).
- **Prerequisites**: Knowledge of Java and Maven, familiarity with actor-based programming (AKKA documentation).
- **Processes**: The system consists of `N` processes with unique identifiers. Each process interacts asynchronously, and `f` processes may be crash-prone.
- **Channels**: Reliable and asynchronous communication between processes.

## Implementation Details
### Main Components:
1. **`Process` Class**: 
   - Implements the `propose(v)` method for proposing values.
   - Handles received messages and processes responses using AKKA actors.
2. **Fault Simulation**:
   - A subset `f` of processes are selected to potentially crash.
   - Processes in a fault-prone mode have a probability `α` of crashing after processing events.
3. **Leader Election**:
   - Emulates leader election using timeouts (`tle`) and coordination among processes.
   - Ensures that processes halt proposing after a hold message is received.

### Experimentation:
- Simulate various configurations of `N` and `tle` to measure consensus latency.
- Assess the effect of failure probability `α` on latency.
- Perform five repetitions for each configuration to obtain average latency data.

## How to Run
1. Clone the project repository and build it using Maven.
2. Run the main simulation by starting the `Process` actors and sending the `launch` message.
3. Configure parameters for `N`, `f`, `tle`, and `α` as needed.
4. Analyze logs generated by `LoggingAdapter` for performance metrics.

## Performance Analysis
Our report includes:
- Plots demonstrating how consensus latency varies with `N` (system size), `tle` (leader election timeout), and `α` (failure probability).
- Observations on the optimal conditions for achieving minimal latency under varying system constraints.

## Results and Observations
- **Baseline Configurations**: `N = 3, 10, 100`, `f = 1, 4, 49`, `α = 0, 0.1, 1`.
- **Leader Election Timeout Analysis**: Start with large `tle` and decrease incrementally.
- **Latency Trends**: Discussed in relation to contention, fault tolerance, and the impact of leader election delays.

## Contact
For further details or questions, please reach out to Prof. Pierre Sutra at [pierre.sutra@telecom-sudparis.eu](mailto:pierre.sutra@telecom-sudparis.eu).
