function redstonePublish()
    print("hi")
    table = {}
    table[1] = "message data"
    node:publish(table,"test")
end

function messageCallback(message,type)
    print(message[1])
end
-- sdf
node:onMessage(messageCallback)
node:onRedstoneSignal(redstonePublish)
