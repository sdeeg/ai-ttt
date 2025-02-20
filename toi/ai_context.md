# AI Context Guide

## Metadata
```yaml
project_name: Tic Tac Toe AI
version: 1.0.0
last_updated: 2024-02-20
primary_language: Kotlin
framework: Spring Boot
architecture_type: Two-tier
documentation_type: AI Context Guide
implementation_status: Complete
test_coverage: 0%
```

## Quick Reference
### Key Project Facts
- Two-player Tic Tac Toe game with AI opponent
- RESTful API with Spring Boot backend
- Minimax algorithm with alpha-beta pruning for AI moves
- Thread-safe game state management
- HTML/JS frontend with real-time updates

### Critical Files and Their Purposes
```yaml
domain:
  - Board.kt: Core game board representation and move validation
  - GameState.kt: Game state management and transitions

engine:
  - GameEngine.kt: Game logic and AI move generation

service:
  - GameService.kt: Business logic and game lifecycle management

api:
  - GameController.kt: REST endpoints and request handling

frontend:
  - index.html: Game board UI
  - game.js: Frontend logic and API integration
  - styles.css: Visual styling
```

## Implementation Guidelines

### State Management
- All game state changes must be immutable
- Use ConcurrentHashMap for thread-safe storage
- Validate state transitions before applying
- Preserve state history for AI analysis

### Decision Points
```yaml
move_validation:
  location: GameEngine.validateMove()
  considerations:
    - Check if position is within bounds
    - Verify cell is empty
    - Confirm game is not complete
    - Validate correct player turn

win_detection:
  location: GameEngine.checkWinCondition()
  considerations:
    - Check rows, columns, diagonals
    - Handle draw conditions
    - Update game status

ai_move_generation:
  location: GameEngine.generateAiMove()
  considerations:
    - Use minimax with alpha-beta pruning
    - Respect depth limit (default: 6)
    - Consider performance constraints
    - Handle edge cases

game_status_updates:
  location: GameEngine.determineGameStatus()
  considerations:
    - Track game progression
    - Handle abandonment
    - Manage completion states
```

### Performance Boundaries
```yaml
response_times:
  api: < 100ms
  ai_move: < 1s
  state_updates: < 50ms

resource_limits:
  memory_per_game: < 1MB
  ai_search_depth: 6 levels
  concurrent_games: Limited by available memory
  connection_pool: Spring Boot default
```

### Common Task Patterns

#### Adding New Game Features
1. Update domain models in Board.kt/GameState.kt
2. Implement logic in GameEngine.kt
3. Add service methods in GameService.kt
4. Create API endpoints in GameController.kt
5. Update frontend in game.js
6. Add tests at each layer

#### Modifying AI Behavior
1. Locate GameEngine.minimax() method
2. Adjust evaluation functions
3. Update pruning parameters
4. Add performance tests
5. Validate against win rate requirements

#### State Management Changes
1. Update GameState.kt with new fields
2. Modify state transitions in GameEngine.kt
3. Update storage in GameService.kt
4. Add validation in GameController.kt
5. Update frontend state handling

## Integration Points
```yaml
frontend_to_backend:
  protocol: REST
  endpoints:
    - POST /api/game: Create new game
    - GET /api/game/{id}: Get game state
    - POST /api/game/{id}/move: Make move
    - POST /api/game/{id}/ai-move: Request AI move

persistence:
  type: In-memory
  implementation: ConcurrentHashMap
  considerations:
    - Thread safety
    - Memory limits
    - Cleanup strategy
```

## Error Handling
```yaml
validation_errors:
  - Invalid move positions
  - Out of turn moves
  - Moves in completed games
  - Invalid game IDs

system_errors:
  - Concurrent modification conflicts
  - Memory constraints
  - Performance degradation
  - Invalid state transitions
```

## Testing Requirements
```yaml
unit_tests:
  coverage_target: 80%
  critical_areas:
    - Move validation
    - Win detection
    - AI move generation
    - State transitions

integration_tests:
  coverage_target: 70%
  focus_areas:
    - API endpoints
    - Game flow
    - Error handling
    - Concurrent access

performance_tests:
  thresholds:
    - API response time: 100ms
    - AI move generation: 1s
    - Concurrent games: 100
```

## Documentation Map
- [Architecture Details](architecture.md)
- [Implementation Status](implementation_state.md)
- [Project Tracking](project_tracking.md)

## Common AI Tasks
1. Game state analysis
2. Move validation
3. Win condition checking
4. AI move generation
5. Performance optimization
6. Error handling
7. State management
8. Frontend integration

## System Constraints
```yaml
technical:
  - Spring Boot framework
  - Kotlin language
  - RESTful API
  - Thread-safe operations

operational:
  - Response time limits
  - Memory constraints
  - Concurrent user support
  - Browser compatibility
```

See other documentation files for detailed information on specific aspects of the system.
