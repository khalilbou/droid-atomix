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

import edu.rit.poe.atomix.levels.Atom;
import edu.rit.poe.atomix.levels.Level;
import edu.rit.poe.atomix.levels.LevelManager;
import edu.rit.poe.atomix.levels.Square;

/**
 * A Java Bean style class to hold and persist game state for a single user.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class GameState {
    
    public static enum Direction {
        
        UP,
        
        DOWN,
        
        RIGHT,
        
        LEFT;
        
    } // Direction
    
    private Level currentLevel;
    
    private Square[][] board;
    
    private Atom selected;
    
    /**
     * Constructs a new <tt>GameState</tt>.
     * 
     * @param   user    the user that this game state exists for
     */
    public GameState( User user ) {
        
        // load the initial level for game state
        LevelManager levelManager = LevelManager.getInstance();
        currentLevel = levelManager.getStartingLevel();
        
        // make a copy of the level's board, for own own use and modification
        board = currentLevel.copyBoard();
    }

    public Square[][] getBoard() {
        return board;
    }
    
    public Atom move( int x, int y, Direction direction )
            throws GameException {
        if ( ! ( board[ y ][ x ] instanceof Atom ) ) {
            throw new GameException( "No atom at specified location." );
        }
        
        
        
        return ( Atom )board[ y ][ x ];
    }
    
    
    public Atom getSelected() {
        return selected;
    }
    
} // GameState
