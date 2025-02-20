# API Contract

## Metadata
```yaml
version: 1.0.0
last_updated: 2024-02-20
base_path: /api
content_type: application/json
authentication: None
rate_limit: None
```

## OpenAPI Specification
```yaml
openapi: 3.0.0
info:
  title: Tic Tac Toe API
  version: 1.0.0
  description: REST API for Tic Tac Toe game with AI opponent

servers:
  - url: http://localhost:8080/api
    description: Local development server

paths:
  /game:
    post:
      summary: Create new game
      operationId: createGame
      responses:
        '200':
          description: Game created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/GameState'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /game/{id}:
    get:
      summary: Get game state
      operationId: getGame
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Game state retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/GameState'
        '404':
          description: Game not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /game/{id}/move:
    post:
      summary: Make a move
      operationId: makeMove
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MoveRequest'
      responses:
        '200':
          description: Move made successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/GameState'
        '400':
          description: Invalid move
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '404':
          description: Game not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '409':
          description: Invalid game state for move
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /game/{id}/ai-move:
    post:
      summary: Request AI move
      operationId: makeAiMove
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: AI move made successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/GameState'
        '404':
          description: Game not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '409':
          description: Invalid game state for AI move
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

components:
  schemas:
    Player:
      type: string
      enum: [X, O]

    Position:
      type: object
      properties:
        row:
          type: integer
          minimum: 0
          maximum: 2
        col:
          type: integer
          minimum: 0
          maximum: 2
      required:
        - row
        - col

    Move:
      type: object
      properties:
        player:
          $ref: '#/components/schemas/Player'
        position:
          $ref: '#/components/schemas/Position'
        timestamp:
          type: string
          format: date-time
      required:
        - player
        - position
        - timestamp

    MoveRequest:
      type: object
      properties:
        position:
          $ref: '#/components/schemas/Position'
      required:
        - position

    GameStatus:
      type: string
      enum: [NEW, IN_PROGRESS, COMPLETED, ABANDONED]

    Cell:
      type: object
      properties:
        position:
          $ref: '#/components/schemas/Position'
        player:
          $ref: '#/components/schemas/Player'
      required:
        - position

    Board:
      type: object
      properties:
        cells:
          type: array
          items:
            type: array
            items:
              $ref: '#/components/schemas/Cell'
        currentPlayer:
          $ref: '#/components/schemas/Player'
        moveCount:
          type: integer
          minimum: 0
          maximum: 9
      required:
        - cells
        - currentPlayer
        - moveCount

    GameState:
      type: object
      properties:
        id:
          type: string
        board:
          $ref: '#/components/schemas/Board'
        status:
          $ref: '#/components/schemas/GameStatus'
        winner:
          $ref: '#/components/schemas/Player'
        lastMove:
          $ref: '#/components/schemas/Move'
      required:
        - id
        - board
        - status

    ErrorResponse:
      type: object
      properties:
        code:
          type: string
        message:
          type: string
        details:
          type: object
          additionalProperties: true
      required:
        - code
        - message
```

## Example Requests and Responses

### Create Game
```http
POST /api/game
```

Response:
```json
{
  "id": "game123",
  "board": {
    "cells": [
      [{"position": {"row": 0, "col": 0}}, {"position": {"row": 0, "col": 1}}, {"position": {"row": 0, "col": 2}}],
      [{"position": {"row": 1, "col": 0}}, {"position": {"row": 1, "col": 1}}, {"position": {"row": 1, "col": 2}}],
      [{"position": {"row": 2, "col": 0}}, {"position": {"row": 2, "col": 1}}, {"position": {"row": 2, "col": 2}}]
    ],
    "currentPlayer": "X",
    "moveCount": 0
  },
  "status": "NEW"
}
```

### Make Move
```http
POST /api/game/game123/move
Content-Type: application/json

{
  "position": {
    "row": 1,
    "col": 1
  }
}
```

Response:
```json
{
  "id": "game123",
  "board": {
    "cells": [
      [{"position": {"row": 0, "col": 0}}, {"position": {"row": 0, "col": 1}}, {"position": {"row": 0, "col": 2}}],
      [{"position": {"row": 1, "col": 0}}, {"position": {"row": 1, "col": 1}, "player": "X"}, {"position": {"row": 1, "col": 2}}],
      [{"position": {"row": 2, "col": 0}}, {"position": {"row": 2, "col": 1}}, {"position": {"row": 2, "col": 2}}]
    ],
    "currentPlayer": "O",
    "moveCount": 1
  },
  "status": "IN_PROGRESS",
  "lastMove": {
    "player": "X",
    "position": {"row": 1, "col": 1},
    "timestamp": "2024-02-20T18:40:00Z"
  }
}
```

## Error Scenarios

### Invalid Move
```http
POST /api/game/game123/move
Content-Type: application/json

{
  "position": {
    "row": 1,
    "col": 1
  }
}
```

Response:
```json
{
  "code": "INVALID_MOVE",
  "message": "Position already occupied",
  "details": {
    "position": {"row": 1, "col": 1},
    "currentPlayer": "O"
  }
}
```

### Game Not Found
```http
GET /api/game/invalid123
```

Response:
```json
{
  "code": "GAME_NOT_FOUND",
  "message": "Game with ID 'invalid123' not found"
}
```

## Rate Limits and Quotas
- No rate limits currently implemented
- Consider implementing if needed:
  - Per-IP rate limiting
  - Concurrent game limits
  - Move frequency limits

## Authentication and Authorization
- No authentication currently required
- Future considerations:
  - User accounts
  - Session management
  - Move validation per user

## Error Codes
```yaml
validation_errors:
  INVALID_MOVE: Move position is invalid or cell occupied
  INVALID_TURN: Not the player's turn
  GAME_COMPLETED: Game is already complete
  INVALID_POSITION: Position coordinates out of bounds

system_errors:
  GAME_NOT_FOUND: Requested game does not exist
  INTERNAL_ERROR: Unexpected server error
  CONCURRENT_MODIFICATION: Game state was modified concurrently
```

## Performance Considerations
- API response time target: < 100ms
- AI move generation time: < 1s
- Concurrent request handling: Thread-safe
- Connection pooling: Spring Boot default
