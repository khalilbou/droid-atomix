/*
 * Atomix.java
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

package edu.rit.poe.atomix;

/**
 * Singleton controller for the application that stores all global state that is
 * unnecessary to load at every pause/restore state event.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Atomix {
    
    private volatile static Atomix instance;
    
    public static Atomix getAtomix() {
        // doubled-checked locking anti-pattern is avoided using volatile
        if ( instance == null ) {
            synchronized ( Atomix.class ) {
                if ( instance == null ) {
                    instance = new Atomix();
                }
            }
        }
        return instance;
    }
    
    /**
     * Constructs a new <tt>Atomix</tt>.
     */
    private Atomix() {
    }
    
} // Atomix
