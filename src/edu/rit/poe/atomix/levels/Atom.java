/*
 * Atom.java
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

import java.util.Set;

/**
 * This class represents an individual atom.  This class stores the current
 * location, ID, atom character and color of an atom.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public final class Atom extends Square {
    
    /** The short ID of this atom. */
    private short id;
    
    /** The color of this atom. */
    private String color;
    
    /** The atom character. */
    private char element;
    
    /** The connectors associated with this atom. */
    private Set<Connector> connectors;
    
    /** The X coordinate of this atom. */
    private int x;
    
    /** The Y coordinate of this atom. */
    private int y;
    
    /**
     * Constructs a new <tt>Atom</tt>.
     * 
     * @param   id          the atom ID
     * @param   color       the color of the atom
     * @param   element     the element letter
     * @param   connectors  the atom connectors
     */
    public Atom( short id, String color, char element,
            Set<Connector> connectors ) {
        this.id = id;
        this.color = color;
        this.element = element;
        this.connectors = connectors;
    }

    public short getId() {
        return id;
    }
    
    public String getColor() {
        return color;
    }
    
    public char getElement() {
        return element;
    }
    
    public Set<Connector> getConnectors() {
        return connectors;
    }

    public int getX() {
        return x;
    }

    public void setX( int x ) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY( int y ) {
        this.y = y;
    }
    
    /**
     * Returns this atom in <tt>String</tt> form.
     * 
     * @return  this atom as a <tt>String</tt>
     */
    @Override
    public String toString() {
        return Character.toString( element );
    }
    
} // Atom
