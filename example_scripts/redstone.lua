state = false
function onMessage(message,type)
    print("recieved")
    state = not state
    node:setRedstonePower(state)
    node:setInfoLine(tostring(state))
end

node:onMessage(onMessage)