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
import edu.rit.poe.atomix.levels.Square;
import java.util.logging.Level;

/**
 * 
 * 
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
    
    /**
     * Constructs a new <tt>GameState</tt>.
     */
    public GameState() {
    }
    
    public Atom move( int x, int y, Direction direction )
            throws GameException {
        if ( ! ( board[ y ][ x ] instanceof Atom ) ) {
            throw new GameException( "No molecule at specified location." );
        }
        
        
        
        return null;
    }
    
} // GameState
