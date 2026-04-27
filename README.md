# logpulse

A tail-and-filter utility for aggregating structured logs from multiple services into a unified terminal view.

---

## Installation

```bash
git clone https://github.com/yourname/logpulse.git
cd logpulse && ./mvnw install -q && mv target/logpulse.jar /usr/local/lib/
```

Add the following alias to your shell profile for convenience:

```bash
alias logpulse="java -jar /usr/local/lib/logpulse.jar"
```

---

## Usage

Tail logs from one or more services and filter by log level or keyword:

```bash
# Watch all services, show only ERROR and WARN entries
logpulse --services auth-service,payment-service --level ERROR,WARN

# Tail a single log file and filter by keyword
logpulse --file /var/log/api-gateway.log --grep "timeout"

# Aggregate multiple sources with JSON output formatting
logpulse --services auth-service,inventory --format json --follow
```

**Common flags:**

| Flag | Description |
|------|-------------|
| `--services` | Comma-separated list of service names to watch |
| `--file` | Path to a specific log file |
| `--level` | Filter by log level (DEBUG, INFO, WARN, ERROR) |
| `--grep` | Filter output by keyword or regex pattern |
| `--format` | Output format: `text` (default) or `json` |
| `--follow` | Continuously tail incoming log entries |

---

## Requirements

- Java 17 or higher
- Maven 3.8+

---

## Contributing

Pull requests are welcome. Please open an issue first to discuss any significant changes.

---

## License

This project is licensed under the [MIT License](LICENSE).