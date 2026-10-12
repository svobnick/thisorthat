# Builds the server and puts the one binary it consists of on a bare Debian.
FROM rust:1-slim AS build
WORKDIR /app
COPY Cargo.toml Cargo.lock ./
COPY src src
COPY migrations migrations
RUN cargo build --release --locked

FROM debian:trixie-slim
COPY --from=build /app/target/release/thisorthat-api /usr/local/bin/thisorthat-api
ENV BIND=0.0.0.0:8787
EXPOSE 8787
USER nobody
CMD ["thisorthat-api"]
