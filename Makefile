install:
	mvn clean install

install-quick:
	mvn clean install -DskipTests

test:
	mvn test

javadoc:
	mvn javadoc:jar


run-terminal: install-quick
	mkdir -p dominoes-terminal-game
	cp dominoes-terminal/target/dominoes-terminal-*-dist.tar.gz dominoes-terminal-game/dominoes-terminal.tar.gz
	tar -xzvf dominoes-terminal-game/dominoes-terminal.tar.gz -C dominoes-terminal-game
	clear
	sh dominoes-terminal-game/bin/terminal-dominoes

docker-start:
	docker-compose up -d

docker-stop:
	docker-compose stop

docker-remove:
	docker-compose down

docker-build: install-quick
	docker-compose build

docker-quick-deploy: install-quick
	docker cp dominoes-browser/target/dominoes-browser.war dominoes:/var/lib/jetty/webapps

