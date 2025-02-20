# Testing Guide

## Metadata
```yaml
version: 1.0.0
last_updated: 2024-02-20
coverage_target: 80%
priority: High
status: Required
documentation_type: Testing Guide
```

## Test Categories

### Unit Tests
```yaml
framework: JUnit 5
coverage_target: 80%
focus_areas:
  domain_layer:
    - Board state management
    - Move validation
    - Win condition detection
    - Game state transitions
    
  game_engine:
    - AI move generation
    - Position evaluation
    - Minimax implementation
    - Alpha-beta pruning
    
  service_layer:
    - Game creation
    - Move processing
    - State management
    - Cleanup operations
    
  api_layer:
    - Request validation
    - Response formatting
    - Error handling
    - Resource management
```

### Integration Tests
```yaml
framework: Spring Boot Test
coverage_target: 70%
focus_areas:
  api_integration:
    - End-to-end request flow
    - Error response handling
    - Content negotiation
    - Status code verification
    
  service_integration:
    - Component interaction
    - State consistency
    - Transaction handling
    - Event propagation
    
  frontend_integration:
    - API consumption
    - State management
    - Event handling
    - Error display
```

### Performance Tests
```yaml
framework: JMeter
thresholds:
  api_response_time: < 100ms
  ai_move_generation: < 1s
  concurrent_users: 100
  error_rate: < 1%

test_scenarios:
  - Load testing
  - Stress testing
  - Endurance testing
  - Spike testing
```

## Test Data Generation

### Board States
```kotlin
fun generateTestBoards(): List<Board> {
    return listOf(
        emptyBoard(),
        boardWithOneMove(),
        boardWithMultipleMoves(),
        winningBoard(),
        drawBoard(),
        invalidBoard()
    )
}

fun generateMoveSequences(): List<List<Move>> {
    return listOf(
        validMoveSequence(),
        invalidMoveSequence(),
        winningMoveSequence(),
        drawMoveSequence()
    )
}
```

### Game States
```kotlin
fun generateGameStates(): List<GameState> {
    return listOf(
        newGame(),
        inProgressGame(),
        completedGame(),
        abandonedGame(),
        invalidGame()
    )
}
```

## Test Patterns

### Domain Layer Tests
```kotlin
@Test
fun `given empty board when valid move then updates correctly`() {
    // Arrange
    val board = emptyBoard()
    val move = Move(Player.X, Position(1, 1))
    
    // Act
    val result = board.makeMove(move)
    
    // Assert
    assertThat(result.cells[1][1].player).isEqualTo(Player.X)
    assertThat(result.moveCount).isEqualTo(1)
}

@Test
fun `given winning move when processed then detects win`() {
    // Arrange
    val board = almostWinningBoard()
    val move = winningMove()
    
    // Act
    val result = board.makeMove(move)
    
    // Assert
    assertThat(result.getWinner()).isEqualTo(Player.X)
}
```

### Game Engine Tests
```kotlin
@Test
fun `given game state when generating AI move then returns valid position`() {
    // Arrange
    val gameState = createGameState()
    
    // Act
    val position = gameEngine.generateAiMove(gameState)
    
    // Assert
    assertThat(position).isValidPosition()
    assertThat(gameState.board.isValidMove(Move(Player.O, position))).isTrue()
}

@Test
fun `given multiple possible moves when generating AI move then chooses optimal`() {
    // Arrange
    val gameState = createGameStateWithMultipleOptions()
    
    // Act
    val position = gameEngine.generateAiMove(gameState)
    
    // Assert
    assertThat(position).isOptimalMove()
}
```

### Service Layer Tests
```kotlin
@Test
fun `given new game request when created then initializes correctly`() {
    // Arrange
    val gameService = GameService(gameEngine)
    
    // Act
    val result = gameService.createGame()
    
    // Assert
    assertThat(result.status).isEqualTo(GameStatus.NEW)
    assertThat(result.board).isEmptyBoard()
}

@Test
fun `given concurrent moves when processed then maintains consistency`() {
    // Arrange
    val gameService = GameService(gameEngine)
    val gameId = gameService.createGame().id
    
    // Act & Assert
    runConcurrently {
        makeMove(gameId, Position(0, 0))
        makeMove(gameId, Position(1, 1))
    }
}
```

### API Layer Tests
```kotlin
@Test
fun `given valid move request when processed then returns updated state`() {
    // Arrange
    val gameId = "test-game"
    val moveRequest = MoveRequest(Position(1, 1))
    
    // Act
    val response = mockMvc.perform(post("/api/game/$gameId/move")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(moveRequest)))
        
    // Assert
    response.andExpect(status().isOk)
           .andExpect(jsonPath("$.board.cells[1][1].player").value("X"))
}

@Test
fun `given invalid move request when processed then returns error`() {
    // Arrange
    val gameId = "test-game"
    val invalidRequest = MoveRequest(Position(5, 5))
    
    // Act
    val response = mockMvc.perform(post("/api/game/$gameId/move")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
        
    // Assert
    response.andExpect(status().isBadRequest)
           .andExpect(jsonPath("$.code").value("INVALID_POSITION"))
}
```

## Mocking Strategies

### Game Engine Mocking
```kotlin
@MockBean
private lateinit var gameEngine: GameEngine

@Test
fun `given mocked engine when generating move then uses configuration`() {
    // Arrange
    whenever(gameEngine.generateAiMove(any()))
        .thenReturn(Position(1, 1))
    
    // Act
    val result = gameService.makeAiMove(gameId)
    
    // Assert
    verify(gameEngine).generateAiMove(any())
    assertThat(result.lastMove.position).isEqualTo(Position(1, 1))
}
```

### Service Layer Mocking
```kotlin
@MockBean
private lateinit var gameService: GameService

@Test
fun `given mocked service when handling request then processes correctly`() {
    // Arrange
    val gameState = createTestGameState()
    whenever(gameService.getGame(any())).thenReturn(gameState)
    
    // Act & Assert
    mockMvc.perform(get("/api/game/test-id"))
           .andExpect(status().isOk)
           .andExpect(jsonPath("$.id").value(gameState.id))
}
```

## Performance Test Scenarios

### Load Testing
```yaml
scenarios:
  create_game:
    users: 50
    ramp_up: 30s
    duration: 5m
    thresholds:
      response_time: < 100ms
      error_rate: < 1%

  make_move:
    users: 100
    ramp_up: 1m
    duration: 10m
    thresholds:
      response_time: < 200ms
      error_rate: < 1%

  ai_move:
    users: 25
    ramp_up: 30s
    duration: 5m
    thresholds:
      response_time: < 1s
      error_rate: < 1%
```

### Stress Testing
```yaml
scenarios:
  concurrent_games:
    initial_users: 100
    max_users: 1000
    increment: 100
    step_duration: 1m
    thresholds:
      error_rate: < 5%
      response_time: < 500ms
```

## Test Coverage Requirements

### Code Coverage
```yaml
overall_target: 80%
critical_areas:
  game_engine: 90%
  move_validation: 90%
  win_detection: 90%
  state_management: 85%

tools:
  - JaCoCo
  - Kotlin Coverage
```

### Functional Coverage
```yaml
game_features:
  - Board initialization
  - Move validation
  - Win detection
  - Draw detection
  - AI move generation
  - Game state management
  - Error handling
  - Concurrent access

edge_cases:
  - Invalid moves
  - Out of bounds
  - Game completion
  - Concurrent modifications
  - Resource exhaustion
```

## Continuous Integration

### Test Execution
```yaml
triggers:
  - Pull request
  - Main branch push
  - Release tag

stages:
  - Unit tests
  - Integration tests
  - Performance tests
  - Coverage analysis
```

### Quality Gates
```yaml
requirements:
  - All tests pass
  - Coverage targets met
  - No critical issues
  - Performance thresholds met
```

## Test Environment

### Configuration
```yaml
test_environment:
  java_version: 11
  memory: 512MB
  timezone: UTC
  locale: en_US

database:
  type: In-memory
  cleanup: After each test
```

### Test Data
```yaml
data_sets:
  - Empty boards
  - Partial games
  - Completed games
  - Edge cases
  - Invalid states
```

## Best Practices

### Test Structure
```yaml
naming_convention:
  pattern: "given [context] when [action] then [result]"
  example: "given empty board when valid move then updates correctly"

organization:
  - Arrange (setup)
  - Act (execution)
  - Assert (verification)
```

### Test Independence
```yaml
requirements:
  - No shared state
  - No test order dependencies
  - Isolated resources
  - Cleanup after each test
```

### Error Handling
```yaml
verification:
  - Expected exceptions
  - Error messages
  - State consistency
  - Resource cleanup
```

## Reporting

### Coverage Reports
```yaml
metrics:
  - Line coverage
  - Branch coverage
  - Method coverage
  - Class coverage

formats:
  - HTML
  - XML
  - Console
```

### Test Reports
```yaml
contents:
  - Test results
  - Execution time
  - Failure details
  - Coverage metrics
