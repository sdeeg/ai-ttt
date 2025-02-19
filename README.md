# AI Tic Tac Toe

A sophisticated implementation of Tic Tac Toe with an AI opponent using the Minimax algorithm with Alpha-Beta pruning. Built with Spring Boot and Kotlin.

## Features

- Single-player gameplay against AI
- Optimized AI using Minimax algorithm with Alpha-Beta pruning
- RESTful API for game state management
- Responsive web interface
- Real-time game state updates
- Proper game state management and validation

## Technology Stack

- Backend:
  - Kotlin
  - Spring Boot
  - Maven
- Frontend:
  - HTML5
  - CSS3
  - JavaScript (ES6+)
- AI:
  - Minimax algorithm
  - Alpha-Beta pruning optimization

## Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.6 or higher

### Running the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/sdeeg/ai-ttt.git
   cd ai-ttt
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

## API Documentation

### Endpoints

#### Create New Game
```http
POST /api/game
Response: GameState
```

#### Get Game State
```http
GET /api/game/{id}
Response: GameState
```

#### Make Move
```http
POST /api/game/{id}/move
Request Body: { "row": number, "col": number }
Response: GameState
```

#### Request AI Move
```http
POST /api/game/{id}/ai-move
Response: GameState
```

#### Abandon Game
```http
POST /api/game/{id}/abandon
Response: GameState
```

### Response Types

#### GameState
```json
{
  "id": "string",
  "board": [
    [
      {
        "position": { "row": number, "col": number },
        "player": "X" | "O" | null
      }
    ]
  ],
  "currentPlayer": "X" | "O",
  "status": "NEW" | "IN_PROGRESS" | "COMPLETED" | "ABANDONED",
  "winner": "X" | "O" | null,
  "lastMove": {
    "player": "X" | "O",
    "position": { "row": number, "col": number },
    "timestamp": "string"
  }
}
```

## AI Implementation

The AI uses the Minimax algorithm with Alpha-Beta pruning to determine the best possible move. The implementation includes:

- Depth-limited search (configurable, default: 6)
- Position evaluation heuristics
- Move ordering optimization
- Early termination for winning positions

### Evaluation Strategy

The AI evaluates positions based on:
- Center control (3 points)
- Corner control (2 points each)
- Winning positions (10 points)
- Depth consideration for faster wins

## Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   └── us/planet10/ai/ttt/
│   │       ├── api/           # REST Controllers
│   │       ├── domain/        # Core domain models
│   │       ├── engine/        # Game logic & AI
│   │       └── service/       # Business logic
│   └── resources/
│       └── static/           # Frontend assets
└── test/
    └── kotlin/              # Test suites
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
