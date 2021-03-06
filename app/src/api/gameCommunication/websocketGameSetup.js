import { connectToWebsocket } from "../stomp/webSocketConnection";
import requestNextMovie from "./websocketGameSender"
import axios from "axios";

let stompClient;
let gameSubsribers = [];
let gameId = null;

const startNewGame = function () {
    const url = process.env.REACT_APP_BACKEND_IP
    stompClient = connectToWebsocket();
    stompClient.onConnect = (frame) => {
        axios.get(`http://${url}/game/new`)
            .then((response) => {
                gameId = response.data.gameId;
                stompClient.subscribe(`/topic/game/${gameId}`, handleGameUpdate);
                requestNextMovie(gameId);
            });
    }
}

const handleGameUpdate = function (message) {
    console.log(message)
    if (message.body != null) {
        gameSubsribers.forEach((sub) => {
            sub.updateFunction(message.body);
        });
    }
}

const subscribeToGameUpdates = function (updateFunction) {
    gameSubsribers.push({
        updateFunction: updateFunction,
    });
}

export {startNewGame, subscribeToGameUpdates, gameId};