class TicTacToe {
    constructor() {
        this.gameId = null;
        this.board = document.querySelector('.game-board');
        this.cells = document.querySelectorAll('.cell');
        this.status = document.getElementById('game-status');
        this.newGameButton = document.getElementById('new-game');
        
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        this.cells.forEach(cell => {
            cell.addEventListener('click', () => this.handleCellClick(cell));
        });
        
        this.newGameButton.addEventListener('click', () => this.startNewGame());
        
        // Handle page unload to abandon game
        window.addEventListener('beforeunload', () => {
            if (this.gameId) {
                this.abandonGame();
            }
        });
    }

    async startNewGame() {
        try {
            const response = await fetch('/api/game', {
                method: 'POST'
            });
            if (!response.ok) {
                throw new Error('Failed to start game');
            }
            const game = await response.json();
            this.gameId = game.id;
            this.updateBoard(game);
            this.status.textContent = 'Game started! Your turn (X)';
        } catch (error) {
            console.error('Error starting new game:', error);
            this.status.textContent = 'Error starting game. Please try again.';
        }
    }

    async handleCellClick(cell) {
        if (!this.gameId || this.isGameOver()) {
            return;
        }

        const row = parseInt(cell.dataset.row);
        const col = parseInt(cell.dataset.col);
        
        // Check if cell is already occupied
        const cellState = this.getCellState(row, col);
        if (cellState.player) {
            return;
        }

        try {
            // Make player move
            const moveResponse = await fetch(`/api/game/${this.gameId}/move`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ row, col })
            });
            
            if (!moveResponse.ok) {
                throw new Error('Invalid move');
            }
            
            const gameState = await moveResponse.json();
            this.updateBoard(gameState);

            if (this.isGameOver(gameState)) {
                return;
            }

            // Make AI move
            this.status.textContent = 'AI is thinking...';
            const aiResponse = await fetch(`/api/game/${this.gameId}/ai-move`, {
                method: 'POST'
            });
            
            if (!aiResponse.ok) {
                throw new Error('AI move failed');
            }
            
            const aiGameState = await aiResponse.json();
            this.updateBoard(aiGameState);
        } catch (error) {
            console.error('Error making move:', error);
            this.status.textContent = 'Error making move. Please try again.';
        }
    }

    getCellState(row, col) {
        return this.currentGameState?.board[row][col] || { player: null };
    }

    updateBoard(gameState) {
        this.currentGameState = gameState;
        
        gameState.board.forEach((row, i) => {
            row.forEach((cell, j) => {
                const cellElement = this.board.querySelector(`[data-row="${i}"][data-col="${j}"]`);
                cellElement.textContent = cell.player || '';
                cellElement.className = 'cell ' + (cell.player?.toLowerCase() || '');
            });
        });

        if (gameState.status === 'COMPLETED') {
            if (gameState.winner) {
                this.status.textContent = `Game Over! ${gameState.winner} wins!`;
            } else {
                this.status.textContent = 'Game Over! It\'s a draw!';
            }
        } else if (gameState.status === 'ABANDONED') {
            this.status.textContent = 'Game abandoned';
        } else {
            this.status.textContent = `Current player: ${gameState.currentPlayer}`;
        }

        // Highlight last move if exists
        if (gameState.lastMove) {
            const { row, col } = gameState.lastMove.position;
            const lastMoveCell = this.board.querySelector(`[data-row="${row}"][data-col="${col}"]`);
            lastMoveCell.classList.add('last-move');
        }
    }

    isGameOver(gameState = this.currentGameState) {
        return gameState?.status === 'COMPLETED' || gameState?.status === 'ABANDONED';
    }

    async abandonGame() {
        if (!this.gameId) return;
        
        try {
            await fetch(`/api/game/${this.gameId}/abandon`, {
                method: 'POST'
            });
        } catch (error) {
            console.error('Error abandoning game:', error);
        }
    }
}

// Initialize game when page loads
document.addEventListener('DOMContentLoaded', () => {
    const game = new TicTacToe();
    game.startNewGame();
});
