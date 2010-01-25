/*
 * GameState.java
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
import edu.rit.poe.atomix.levels.Atom;
import edu.rit.poe.atomix.levels.Level;
import edu.rit.poe.atomix.levels.LevelManager;
import edu.rit.poe.atomix.levels.Square;
import edu.rit.poe.atomix.util.Point;
import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

/**
 * This class represents all in-game state.  It wraps the <tt>User</tt> and
 * <tt>Game</tt> objects that are queried from and persisted to the database.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class GameState implements Serializable {
    
    /** The stirng literal key for this object. */
    public static final String GAME_STATE_KEY = "game_state";
    
    /**
     * An enumerated type of directions, useful for the game state.
     * 
     * @author  Peter O. Erickson
     */
    public static enum Direction {
        
        /** The up direction. */
        UP,
        
        /** The down direction. */
        DOWN,
        
        /** The right direction. */
        RIGHT,
        
        /** The left direction. */
        LEFT;
        
    } // Direction
    
    /** The game's current user. */
    User user;
    
    /** The game's current <tt>Game</tt> object. */
    Game game;
    
    /** The game's current board configuration. */
    Square[][] board;
    
    /** The (x,y) location of the currently selected atom. */
    Point selected;
    
    /** The point that is currently being hovered over, or <tt>null</tt>. */
    Point hoverPoint;
    
    /** The stack of moves to potentially be undone. */
    Stack<Move> undoStack;
    
    /** The last starting time of the game play timer. */
    long timeStarted_sec;
    
    /**
     * Constructs a new <tt>GameState</tt> with the specified user and game
     * data objects.
     * 
     * @param   user    the user playing this game
     * @param   game    the backing persistent game object
     */
    public GameState( User user, Game game ) {
        this.user = user;
        this.game = game;
        
        // initialize a default hover point
        hoverPoint = new Point( 0, 0 );
        
        // make a copy of the level's board, for own own use and modification
        board = getLevelObj().copyBoard();
        
        // remove atoms from the board (we'll place atoms from the game object)
        for ( int i = 0; i < board.length; i++ ) {
            for ( int j = 0; j < board[ 0 ].length; j++ ) {
                if ( board[ i ][ j ] instanceof Atom ) {
                    board[ i ][ j ] = Square.EMPTY;
                }
            }
        }
        
        // set the atom locations from the game object
        for ( Map.Entry<Short, Point> entry : game.getAtoms().entrySet() ) {
            Atom atom = getLevelObj().getAtom( entry.getKey() );
            Point point = entry.getValue();
            
            board[ point.y ][ point.x ] = atom;
        }
        
        undoStack = new Stack<Move>();
    }
    
    // Simple Accesors/Mutators
    
    /**
     * Private helper method to access the <tt>Level</tt> object.
     * 
     * @return  the current level's object
     */
    Level getLevelObj() {
        LevelManager levelManager = LevelManager.getInstance();
        return levelManager.getLevel( game.getLevel() );
    }
    
    /**
     * Returns the backing <tt>User</tt> object.
     * 
     * @return  the persistent user object
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Returns the backing <tt>Game</tt> object.
     * 
     * @return  the persistent game object
     */
    public Game getGame() {
        return game;
    }
    
    /**
     * Returns the current level of this game state.
     * 
     * @return  the current level
     */
    public int getLevel() {
        return game.getLevel();
    }
    
    /**
     * Returns the formula for the current level's goal.
     * 
     * @return  the goal molecule's formula
     */
    public String getFormula() {
        return getLevelObj().getFormula();
    }
    
    /**
     * Returns the name of the current level's molecule.
     * 
     * @return  the molecule name for the current level
     */
    public String getMoleculeName() {
        return getLevelObj().getName();
    }
    
    /**
     * Returns the game's board.
     * 
     * @return  the board's current state
     */
    public Square[][] getBoard() {
        return board;
    }
    
    /**
     * Returns the game's goal configuration.
     * <p>
     * This method is a pass-through to the backing <tt>Level</tt> object.
     * 
     * @return  the goal configuration
     */
    public Square[][] getGoal() {
        // consult the gold standard level object
        return getLevelObj().getGoal();
    }
    
    /**
     * Returned the point location of the currently selected object.
     * 
     * @return  the currently selected point
     */
    public Point getSelected() {
        return selected;
    }
    
    /**
     * Sets the currently selected point.
     * 
     * @param   selected    the new selection point
     */
    public void setSelected( Point selected ) {
        this.selected = selected;
    }
    
    /**
     * Returns the current hover point.
     * 
     * @return  the hover point
     */
    public Point getHoverPoint() {
        return hoverPoint;
    }
    
    /**
     * Sets the current hover point.
     * 
     * @param   hoverPoint  the new hover point
     */
    public void setHoverPoint( Point hoverPoint ) {
        this.hoverPoint = hoverPoint;
    }
    
    /**
     * Returns whether the game is completed.
     * 
     * @return  <tt>true</tt> if the game is finished, otherwise <tt>false</tt>
     */
    public boolean isFinished() {
        return game.isFinished();
    }
    
    /**
     * This class is a holder of move information that can be reverted.
     * 
     * @author  Peter O. Erickson
     * 
     * @version $Id$
     */
    static class Move implements Serializable {
        
        /** The starting location of this move. */
        Point start;
        
        /** The ending location of this move. */
        Point end;
        
        /**
         * Constructs a new move with the starting and ending location.
         * 
         * @param   start   the starting location
         * @param   end     the ending location
         */
        Move( Point start, Point end ) {
            this.start = start;
            this.end = end;
        }
        
    } // Move
    
} // GameState
