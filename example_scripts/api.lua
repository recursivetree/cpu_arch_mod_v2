function redstonePublish()
    table = {}
    table[1] = "message data"
    node:publish(table)
end

function messageCallback(message)
    print(message[1])
end

node:onMessage(messageCallback)
node:onRedstoneSignal(redstonePublish)
