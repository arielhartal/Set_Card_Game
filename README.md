# Set Card Game
This project is a simplified version of the well-known card game "Set," with a few unique twists. The objective is to identify groups of three cards that satisfy particular conditions to form a valid set.  

# Introduction
To understand the game's objective and rules, you can refer to the original Set Card Game description (link provided in the project description). Keep in mind that this implementation uses slightly different rules, as outlined in the project specifications.  

https://en.wikipedia.org/wiki/Set_(card_game)  

# Features and Cards
The game uses a deck made up of 81 unique cards. Each card has a drawing characterized by four attributes: color, number, shape, and shading. These attributes come in different predefined options.

# Table and Players
At the start of the game, 12 cards are drawn from the deck and arranged on a 3x4 grid on the table. The objective for players is to identify a combination of three cards on the table that form a "valid set."

A "valid set" is defined by a set of criteria: for each of the four attributes (color, number, shape, and shading), the three selected cards must either all have the same value or all have different values for that particular feature.

Players interact with the game by placing tokens on the cards using specific keys on the keyboard. Each player is assigned 12 distinct keys that correspond to the card slots on the table. Players can use their keys to add or remove tokens from the cards.

The game includes two types of players: human and non-human. Human players interact with the game via physical keyboard input, while non-human players are represented by threads that simulate random key presses.

# The Dealer
The dealer controls the overall game flow. It manages player threads, shuffles and deals cards to the table, collects cards, checks if the tokens form a valid set, and adjusts scores by awarding points or applying penalties. The dealer also keeps track of the countdown timer and verifies if any valid sets remain among the cards on the table and in the deck.

# Graphical User Interface & Keyboard Input
This project includes a graphical interface for the game. Players can interact with it through keyboard input.
