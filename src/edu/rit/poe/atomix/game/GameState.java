/*
 * GameState.java
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
import edu.rit.poe.atomix.levels.Atom;
import edu.rit.poe.atomix.levels.Level;
import edu.rit.poe.atomix.levels.LevelManager;
import edu.rit.poe.atomix.levels.Square;
import edu.rit.poe.atomix.util.Point;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * This class represents all in-game state.  It wraps the <tt>User</tt> and
 * <tt>Game</tt> objects that are queried from and persisted to the database.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class GameState implements Serializable {
    
    public static final String GAME_STATE_KEY = "game_state";
    
    public static final DateFormat DATE_FORMAT =
            new SimpleDateFormat( "MM/dd/yyyy hh:mm a" );
    
    public static enum Direction {
        
        UP,
        
        DOWN,
        
        RIGHT,
        
        LEFT;
        
    } // Direction
    
    User user;
    
    Game game;
    
    Square[][] board;
    
    /** The (x,y) location of the currently selected atom. */
    Point selected;
    
    /** The point that is currently being hovered over, or <tt>null</tt>. */
    Point hoverPoint;
    
    /**
     * 
     * @param user
     * @param game
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
    
    public User getUser() {
        return user;
    }
    
    public Game getGame() {
        return game;
    }
    
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
    
    public Point getSelected() {
        return selected;
    }
    
    public Point getHoverPoint() {
        return hoverPoint;
    }
    
    public void setHoverPoint( Point hoverPoint ) {
        this.hoverPoint = hoverPoint;
    }
    
} // GameState
