/*
 * GameManager.java
 *
 * Version:
 *      $Id$
 *
 * Copyright (c) 2009 Peter O. Erickson
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
    
    /**
     * Creates a new <tt>Game</tt> object for the specified user at the
     * specified level, if such a level exists.
     * 
     * @param   user    the user to create a new game for
     * @param   level   the level at which to create the game
     * 
     * @return          a new <tt>Game</tt> object that has been initialized for
     *                  the specified level
     */
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
        Point start = new Point( endX, endY );
        
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
        Point end = new Point( endX, endY );
        
        // add this move to the undo stack
        gameState.undoStack.clear(); // only keep one move (for now?)
        gameState.undoStack.push( new GameState.Move( start, end ) );
        
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
    
    /**
     * Returns whether an undo operation can be performed.
     * 
     * @param   gameState   the game state to check for an available undo move
     * 
     * @return              <tt>true</tt> if undo can be performed, otherwise
     *                      <tt>false</tt>
     */
    public static boolean canUndo( GameState gameState ) {
        return ( ! gameState.undoStack.empty() );
    }
    
    /**
     * Performs an undo operation, undoing the mostly recently moved atom.
     * 
     * @param   gameState   the game state to perform the undo upon
     */
    public static void undo( GameState gameState ) {
        if ( canUndo( gameState ) ) {
            GameState.Move move = gameState.undoStack.pop();
            
            // move the atom back
            gameState.board[ move.start.y ][ move.start.x ] =
                    gameState.board[ move.end.y ][ move.end.x ];
            gameState.board[ move.end.y ][ move.end.x ] = Square.EMPTY;
            gameState.selected.set( move.start.x, move.start.y );
            gameState.setHoverPoint( move.start );
            
            // update the database copy of the atom's location
            Atom atom = ( Atom )gameState.board[ move.start.y ][ move.start.x ];
            gameState.game.getAtoms().put( atom.getId(),
                    new Point( move.start.x, move.start.y ) );
        }
    }
    
} // GameManager
