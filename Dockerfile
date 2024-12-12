# Use the official Go image as a builder
FROM golang:1.20 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Go module files and download dependencies
COPY go.mod go.sum ./
RUN go mod download

# Copy the rest of the application source code
COPY . ./

# Build the Go application
RUN go build -o main .

# Use a minimal image to run the application
FROM alpine:latest

# Set the working directory inside the container
WORKDIR /root/

# Copy the built application from the builder stage
COPY --from=builder /app/main .

# Expose the port your application runs on
EXPOSE 8080

# Command to run the application
CMD ["./main"]
