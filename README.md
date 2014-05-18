Obsidian
========

Obsidian is a distributed event aggregator.

An event aggregator allows the decoupling of publishers and subscribers.  A comprehensive discussion can be found on
Martin Fowler's [blog](http://martinfowler.com/eaaDev/EventAggregator.html)

I tend to use it a lot in my projects, especially the google implementation found in Guice called
[EventBus](https://code.google.com/p/guava-libraries/wiki/EventBusExplained), however on occasion I have a need to
distribute events across process boundaries.

This project extends event aggregation by intercepting a message event, the message event packages both the actual event
being published and a desired endpoint that will consume it.


