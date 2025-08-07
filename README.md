# Set Card Game

A Java implementation of the Set card game with both human and computer players support. This project was developed as part of a Systems Programming Languages (SPL) course assignment.

## About the Game

Set is a real-time card game where players identify sets of three cards that satisfy specific rules. Each card has four features:
- **Shape**: Oval, Squiggle, or Diamond
- **Color**: Red, Green, or Purple  
- **Number**: One, Two, or Three
- **Shading**: Solid, Striped, or Open

A valid set consists of three cards where each feature is either all the same or all different across the three cards.

## Features

- **Multi-player support**: Mix of human and computer players
- **Real-time gameplay**: Players can place tokens simultaneously
- **Configurable settings**: Customize game parameters via configuration file
- **Visual interface**: Swing-based GUI showing cards and player interactions
- **Thread-safe implementation**: Proper synchronization for concurrent gameplay
- **Comprehensive testing**: Unit tests for core game components

## Project Structure

```
src/
├── main/java/bguspl/set/
│   ├── ex/                    # Main game logic
│   │   ├── Dealer.java        # Game coordinator and card dealer
│   │   ├── Player.java        # Player implementation (human/computer)
│   │   └── Table.java         # Game table state management
│   ├── Main.java              # Application entry point
│   ├── Config.java            # Configuration loader
│   ├── Env.java               # Game environment
│   └── UserInterface*.java    # UI components
├── main/resources/
│   ├── config.properties      # Game configuration
│   └── cards/                 # Card image assets
└── test/java/bguspl/set/ex/   # Unit tests
```

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6 or higher

### Building the Project

```bash
mvn clean compile
```

### Running the Game

```bash
mvn exec:java -Dexec.mainClass="bguspl.set.Main"
```

Or build and run the JAR:

```bash
mvn package
java -jar target/Set_Card_Game-1.0-SNAPSHOT.jar
```

### Running Tests

```bash
mvn test
```

## Configuration

Game settings can be customized in `src/main/resources/config.properties`:

- `HumanPlayers`: Number of human players (keyboard input)
- `ComputerPlayers`: Number of computer players (AI)
- `FeatureCount`: Number of card features (default: 4)
- `FeatureSize`: Number of options per feature (default: 3)
- `TurnTimeoutSeconds`: Time limit for player turns
- `PlayerKeys[i][j]`: Key bindings for human players

## Game Controls

### Human Players
- **Place/Remove Token**: Use assigned keys to place or remove tokens on cards
- **Submit Set**: When you have 3 tokens placed, the dealer will check for a valid set
- **Score Display**: Your current score is shown in the interface

### Gameplay Rules
1. 12 cards are displayed on the table
2. Players place tokens on cards to claim potential sets
3. When a player places 3 tokens, the dealer checks if it's a valid set
4. Valid sets earn points; invalid attempts may result in penalties
5. New cards replace sets that are found
6. Game continues until no more sets are possible

## Implementation Details

### Concurrency Model
- **Dealer Thread**: Manages game state, validates sets, and deals new cards
- **Player Threads**: Handle player input and decision making
- **Synchronization**: Uses monitors and locks to ensure thread-safe operations

### Key Classes
- **`Dealer`**: Central game coordinator, manages the game loop and timing
- **`Player`**: Represents both human and AI players with their strategies
- **`Table`**: Maintains the current state of cards and player tokens
- **`Env`**: Provides shared game environment and configuration

## Development

This project follows object-oriented design principles with:
- Clear separation of concerns
- Thread-safe implementations
- Comprehensive unit testing
- Configuration-driven behavior
- Modular architecture

## Testing

The project includes unit tests for:
- Table state management (`TableTest.java`)
- Player behavior (`PlayerTest.java`) 
- Dealer functionality (`DealerTest.java`)

## License

This project is part of academic coursework and is intended for educational purposes.

## Acknowledgments

- Ben-Gurion University Systems Programming Languages course
- Original Set card game by Marsha Falco
