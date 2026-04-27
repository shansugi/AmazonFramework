package com.amazon.login.stepdefs;

/**
 * SharedContext — shared state passed between step definition classes via PicoContainer.
 *
 * PicoContainer (cucumber-picocontainer dependency) automatically injects this
 * into any step def constructor that requests it. No static fields, no ThreadLocals
 * needed for scenario-level state — PicoContainer creates one instance per scenario.
 *
 * Usage: Add SharedContext as constructor parameter in any step def class.
 */
public class SharedContext {
    public String lastSearchedProduct;
    public String cartTotalBeforeAction;
    public String lastOrderId;
    public int initialCartCount;
    public String currentProductTitle;
    public String currentProductPrice;
}
