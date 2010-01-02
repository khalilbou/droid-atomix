/*
 * Square.java
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
