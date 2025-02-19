package us.planet10.ai.ttt.domain

enum class GameStatus {
    NEW,
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}

data class GameState(
    val id: String,
    val board: Board,
    val status: GameStatus,
    val winner: Player?,
    val lastMove: Move?
) {
    companion object {
        fun create(id: String): GameState {
            return GameState(
                id = id,
                board = GameBoard.createEmpty(),
                status = GameStatus.NEW,
                winner = null,
                lastMove = null
            )
        }
    }
}
