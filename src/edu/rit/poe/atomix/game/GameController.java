/*
 * GameManager.java
 *
 * Version:
 *      $Id$
 *
 * Copyright (c) 2009 Peter O. Erickson
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without major
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, and/or distribute copies of, but
 * under no circumstances sublicense and/or sell, the Software,
 * and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.rit.poe.atomix.game;

import edu.rit.poe.atomix.db.Game;
import edu.rit.poe.atomix.db.User;
import edu.rit.poe.atomix.game.GameState.Direction;
import edu.rit.poe.atomix.levels.Atom;
import edu.rit.poe.atomix.levels.Level;
import edu.rit.poe.atomix.levels.LevelManager;
import edu.rit.poe.atomix.levels.Square;
import edu.rit.poe.atomix.util.Point;
import java.util.Calendar;
import java.util.EnumSet;

/**
 * This class is a controller for the Atomix game in the MVC paradigm.  This
 * class provides static methods to create game state and handle game events.
 * This class does no persistence.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public final class GameController {
    
    /**
     * Creates a new user.
     * 
     * @param   username    the username of the new user to be created
     * 
     * @return              
     */
    public static User newUser( String username ) {
        // create the user
        User user = new User();
        user.setUsername( username );
        
        return user;
    }
    
    public static Game newLevel( User user, int level ) {
        // create basic game data
        Game game = new Game();
        game.setUser( user );
        game.setCreated( Calendar.getInstance() );
        game.setLevel( level );
        game.setSeconds( 0 );
        game.setMoves( 0 );
        game.setFinished( false );
        
        // get the gold standard level
        LevelManager levelManager = LevelManager.getInstance();
        Level goldLevel = levelManager.getLevel( level );
        Square[][] goldBoard = goldLevel.getBoard();
        
        // load the atom locations from the user's saved Game
        Atom atom = null;
        for ( int y = 0; y < goldBoard.length; y++ ) {
            for ( int x = 0; x < goldBoard[ 0 ].length; x++ ) {
                if ( goldBoard[ y ][ x ] instanceof Atom ) {
                    atom = ( Atom )goldBoard[ y ][ x ];
                    
                    // set the atom in the Game
                    game.getAtoms().put( atom.getId(), new Point( x, y ) );
                }
            }
        }
        
        return game;
    }
    
    /**
     * Moves the currently selected atom in the specified direction until an
     * obstacle is hit.
     * 
     * @param   direction       the direction to move the selected atom in
     * 
     * @return                  <tt>true</tt> if this move entered the board
     *                          into a goal state
     * 
     * @throws  GameException   if there is no currently selected atom
     */
    public static boolean moveSelected( GameState gameState,
            GameState.Direction direction ) throws GameException {
        if ( gameState.selected == null ) {
            throw new GameException( "No currently selected atom." );
        }
        int endX = gameState.selected.x;
        int endY = gameState.selected.y;
        
        // perform an iterative search for the furthest distance the atom can
        // travel in the given direction
        int nextX = endX;
        int nextY = endY;
        boolean hit = false;
        while ( ! hit ) {
            // is next move in given direction a wall/end of board?
            switch ( direction ) {
                case RIGHT: {
                    nextX++;
                } break;
                case LEFT: {
                    nextX--;
                } break;
                case DOWN: {
                    nextY++;
                } break;
                case UP: {
                    nextY--;
                } break;
            }
            
            // hit a board boundary?
            if ( ( nextY == gameState.board.length ) ||
                    ( nextX == gameState.board[ 0 ].length ) ) {
                hit = true;
            } else if ( gameState.board[ nextY ][ nextX ]
                    instanceof Square.Wall ) {
                hit = true;
            } else if ( gameState.board[ nextY ][ nextX ] instanceof Atom ) {
                hit = true;
            } else {
                endX = nextX;
                endY = nextY;
            }
        }
        
        //move the atom
        gameState.board[ endY ][ endX ] =
                gameState.board[ gameState.selected.y ][ gameState.selected.x ];
        gameState.board[ gameState.selected.y ][ gameState.selected.x ] =
                Square.EMPTY;
        gameState.selected.set( endX, endY );
        
        // update the game's atom
        Atom atom = ( Atom )gameState.board[ endY ][ endX ];
        gameState.game.getAtoms().put( atom.getId(), new Point( endX, endY ) );
        
        // check for win conditions
        // -- consult the gold standard level object
        return gameState.getLevelObj().isComplete( gameState.board );
    }
    
    /**
     * Returns the possible directions that the currently selected atom can
     * move.
     * 
     * @return  a set of directions that the selected atom can move
     */
    public static EnumSet<Direction> getPossibleDirections(
            GameState gameState ) {
        EnumSet<Direction> directions = EnumSet.noneOf( Direction.class );
        
        if ( gameState.selected != null ) {
            int x = gameState.selected.x;
            int y = gameState.selected.y;
            
            // can we go left?
            if ( ( ( x - 1 ) >= 0 ) && ( gameState.board[ y ][ x - 1 ]
                    instanceof Square.Empty ) ) {
                directions.add( Direction.LEFT );
            }
            
            // can we go right?
            if ( ( ( x + 1 ) < gameState.board[ 0 ].length ) &&
                    ( gameState.board[ y ][ x + 1 ] instanceof
                    Square.Empty ) ) {
                directions.add( Direction.RIGHT );
            }
            
            // can we go up?
            if ( ( ( y - 1 ) >= 0 ) && ( gameState.board[ y - 1 ][ x ]
                    instanceof Square.Empty ) ) {
                directions.add( Direction.UP );
            }
            
            // can we go down?
            if ( ( ( y + 1 ) < gameState.board.length ) &&
                    ( gameState.board[ y + 1 ][ x ]
                    instanceof Square.Empty ) ) {
                directions.add( Direction.DOWN );
            }
        }
        
        return directions;
    }
    
    public static void select( GameState gameState, int x, int y )
            throws GameException {
        if ( gameState.board[ y ][ x ] instanceof Atom ) {
            gameState.selected = new Point( x, y );
        } else {
            throw new GameException( "No atom at specified location." );
        }
    }
    
} // GameManager
