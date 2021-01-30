var ws;
var playerName;

function connect() {
    if (ws != null) {
        return ;
    }

    playerName = document.getElementById("username").value;
    document.getElementById("username").value = "";

    //clear log
    document.getElementById("log").innerHTML = "";

    var host = document.location.host;
    var pathname = document.location.pathname;

    ws = new WebSocket("ws://" +host  + pathname + "dominoes/" + playerName);

    document.getElementById("new_game").style = "display:none";

    ws.onmessage = function(event) { handleMessage(event); };
    ws.onclose = function(event) {
        newGame();
    }
}

function newGame() {
    ws = null;
    document.getElementById("new_game").style = "";
    document.getElementById("players").innerHTML = "";
    document.getElementById("place").style = "display:none";
    document.getElementById("pieces").innerHTML = "";
    document.getElementById("play_moves").style = "display:none";
}

function handleMessage(event) {
    var log = document.getElementById("log");
    console.log(event.data);
    var message = JSON.parse(event.data);
    if (message.type === "WAITING_FOR_PLAYER") {
        log.innerHTML += "Waiting for new player to join\n";
    } else if (message.type === "NEW_GAME") {
        log.innerHTML += "New game started!\n" +
        "The board is: " + boardToString(message.content.board) + "\n";

        fulfilPlayerPieces(message.content.player.pieces);
        fulfilPlayers(message.content.player.name, message.content.otherPlayer, message.content.playingPlayer);

        document.getElementById("stockSize").innerHTML = "Stock size: " + message.content.stockSize;

        document.getElementById("place").style = "";
        if (message.content.player.name == message.content.playingPlayer) {
            log.innerHTML += "Your turn to play, " + message.content.player.name + "\n";
            document.getElementById("play_moves").style = "";
        }


    } else if (message.type === "NEXT_PLAY") {
        log.innerHTML += "The board is: " + boardToString(message.content.board) + "\n";
        fulfilPlayerPieces(message.content.player.pieces);
        fulfilPlayers(message.content.player.name, message.content.otherPlayer, message.content.playingPlayer);
        document.getElementById("stockSize").innerHTML = "Stock size: " + message.content.stockSize;
        if (message.content.player.name == message.content.playingPlayer) {
            log.innerHTML += "Your turn to play, " + message.content.player.name + "\n";
            document.getElementById("play_moves").style = "";
        } else {
            document.getElementById("play_moves").style = "display:none";
        }
    } else if (message.type === "NEW_PIECE_FROM_STOCK") {
        log.innerHTML += "Here's a new piece: " + pieceToString(message.content.newPiece) + "\n";
        fulfilPlayerPieces(message.content.player.pieces);
        document.getElementById("stockSize").innerHTML = "Stock size: " + message.content.stockSize;
    } else if (message.type === "NO_PIECES_ON_STOCK") {
        log.innerHTML += "No Pieces left on stock. You lose your turn\n";
        fulfilPlayers(message.content.player.name, message.content.otherPlayer, message.content.playingPlayer);
    } else if (message.type === "GAME_OVER") {
        log.innerHTML += "The game is over!\n";
        if (message.content.winnerPlayer == "") {
            log.innerHTML += "The game was a draw.\n";
        } else if (message.content.winnerPlayer == playerName) {
            log.innerHTML += "You win, " + playerName + "!!!\n";
        } else {
            log.innerHTML += "You lose, " + playerName + ". The Player " + message.content.winnerPlayer + " got the win.\n";
        }

        ws.close();

    } else if (message.type === "ERROR_MESSAGE") {
         log.innerHTML += "Error: " + message.content.error + "\n";
    }

}

function fulfilPlayers(player, otherPlayer, playingPlayer) {
    var html = "<td>" + player + "</td>";
    html+= "<td> VS </td>";
    html+= "<td>" + otherPlayer + "</td>";
    html+= "<td><b> Player turn: " + playingPlayer;
    if (playingPlayer === player) {
        html+= " (you)";
    }
    html+="</b></td>";
    document.getElementById("players").innerHTML = html;
}

function fulfilPlayerPieces(playerPieces) {
    var str = ""
    for (var i = 0; i < playerPieces.length; i++) {
        str += "<td><input type=\"radio\" name=\"pieces\" id=" + getPieceId(playerPieces[i]) + " value=" + JSON.stringify(playerPieces[i]) +
        " >" + pieceToString(playerPieces[i]) + "</td>";
    }
    document.getElementById("pieces").innerHTML = str;
}

function pieceToSimpleValue(piece) {
    return piece.left + "," + piece.right;
}

function boardToString(board) {
    var retStr = "";
    for (var i = 0; i < board.length; i++) {
        retStr += pieceToString(board[i]);
    }

    return retStr;
}

function pieceToString(piece) {
    return "<" + piece.left + ":" + piece.right + ">";
}

function getPieceId(piece) {
    return "piece_" + piece.left + "_" + piece.right;
}

function play() {
    var pieceToPlay = document.querySelector('input[name="pieces"]:checked');
    var place = document.querySelector('input[name="place"]:checked');
    if (pieceToPlay == null || place == null) {
        console.log("Not a valid play.")
        return;
    }

    var content = {
        "piece": JSON.parse(pieceToPlay.value),
        "place": place.value
    };
    var json = JSON.stringify({
        "type": "PLAY_A_PIECE",
        "content": content
    });

    ws.send(json);

    place.checked = false;
}

function getFromStock() {
    var json = JSON.stringify({
        "type": "GET_FROM_STOCK"
    });

    ws.send(json);
}
