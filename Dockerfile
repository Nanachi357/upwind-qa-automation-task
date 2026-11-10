FROM eclipse-temurin:21-jdk-jammy@sha256:4b1a74fbffbe6b4e7b6f0171a472478282fb463790a51edf10e465d578f22b69

# Keep this aligned with ui.browserVersion in test.properties.
ARG CHROME_VERSION=155.0.8059.84

RUN test "$(dpkg --print-architecture)" = amd64 \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl fonts-liberation tini unzip \
    && curl --fail --location --retry 3 --output /tmp/chrome.zip \
        "https://storage.googleapis.com/chrome-for-testing-public/${CHROME_VERSION}/linux64/chrome-linux64.zip" \
    && curl --fail --location --retry 3 --output /tmp/chromedriver.zip \
        "https://storage.googleapis.com/chrome-for-testing-public/${CHROME_VERSION}/linux64/chromedriver-linux64.zip" \
    && unzip -q /tmp/chrome.zip -d /opt \
    && unzip -q /tmp/chromedriver.zip -d /opt \
    && while read -r dependency; do \
        apt-get satisfy -y --no-install-recommends "$dependency" || exit 1; \
       done < /opt/chrome-linux64/deb.deps \
    && ln -s /opt/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver \
    && rm -f /tmp/chrome.zip /tmp/chromedriver.zip \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --create-home --uid 1000 qa \
    && mkdir /app \
    && chown qa:qa /app

COPY --chmod=755 docker/google-chrome /usr/local/bin/google-chrome

ENV GRADLE_USER_HOME=/home/qa/.gradle
WORKDIR /app
USER qa

COPY --chown=qa:qa gradle/ gradle/
COPY --chown=qa:qa --chmod=755 gradlew ./
COPY --chown=qa:qa build.gradle settings.gradle ./
COPY --chown=qa:qa src/ src/

# Compile and cache dependencies without calling the public test services.
RUN ./gradlew --no-daemon --console=plain testClasses

ENTRYPOINT ["/usr/bin/tini", "--", "./gradlew", "--no-daemon", "--console=plain"]
CMD ["test"]
