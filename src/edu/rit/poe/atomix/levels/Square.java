/*
 * Square.java
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

package edu.rit.poe.atomix.levels;

import java.io.Serializable;

/**
 * This class represents a square on the Atomix board.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Square implements Serializable {
    
    /**
     * A constant for a wall square.
     * 
     * @see     Wall
     */
    public static final Square.Wall WALL = new Square.Wall();
    
    /**
     * A constant for an empty square.
     * 
     * @see     Empty
     */
    public static final Square.Empty EMPTY = new Square.Empty();
    
    /**
     * Constructs a new <tt>Square</tt>.
     */
    protected Square() {
    }
    
    /**
     * This class represents a square that is a wall.  This class should not be
     * constructed; rather, only a single instance of this class is needed.
     * <p>
     * This class exists because simply declaring one static <tt>Square</tt>
     * object as a "wall" type will not pass an <tt>==</tt> operation once the
     * object has been deserialized from persistent storage.
     * 
     * @author  Peter O. Erickson
     * 
     * @version $Id$
     * 
     * @see     Empty
     */
    public static class Wall extends Square {
        
        /**
         * Construct a new object (disabled).
         */
        private Wall() { }
        
    } // Wall
    
    /**
     * This class represents a square that is an empty square.  This class
     * should not be constructed; rather, only a single instance of this class
     * is needed.
     * <p>
     * This class exists because simply declaring one static <tt>Square</tt>
     * object as an "empty square" type will not pass an <tt>==</tt> operation
     * once the object has been deserialized from persistent storage.
     * 
     * @author  Peter O. Erickson
     * 
     * @version $Id$
     * 
     * @see     Wall
     */
    public static class Empty extends Square {
        
        /**
         * Construct a new object (disabled).
         */
        private Empty() { }
        
    } // Empty
    
} // Square
