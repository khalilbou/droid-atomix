/*
 * Point.java
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

package edu.rit.poe.atomix.util;

import java.io.Serializable;

/**
 * A serializable point class.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Point implements Serializable {
    
    /** The X coordinate. */
    public int x;
    
    /** The Y coordinate. */
    public int y;
    
    /**
     * Constructs a new <tt>Point</tt> (0,0).
     */
    public Point() {
        this( 0, 0 );
    }
    
    /**
     * Constructs a new <tt>Point</tt> at the specified location.
     * 
     * @param   x   the X coordinate
     * @param   y   the Y coordinate
     */
    public Point( int x, int y ) {
        set( x, y );
    }
    
    /**
     * Sets a new location of this object.
     * 
     * @param   x   the X coordinate
     * @param   y   the Y coordinate
     */
    public void set( int x, int y ) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Offsets this point's location by <tt>x</tt> and <tt>y</tt>.
     * 
     * @param   x   
     * @param   y   
     */
    public void offset( int x, int y ) {
        this.x += x;
        this.y += y;
    }
    
    public boolean equals( int x, int y ) {
        return ( ( this.x == x ) && ( this.y == y ) );
    }
    
} // Point
