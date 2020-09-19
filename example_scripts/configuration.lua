counter = 0
function onSave()
    node:save(tostring(counter))
end

function onRedstonePowered()
    counter=counter+1
    node:setInfoLine(tostring(counter))
end

function load(data)
    print(data)
    counter = tonumber(data)
    node:setInfoLine(counter)
end

node:onRedstoneSignal(onRedstonePowered)
node:onSave(onSave)
node:onConfigurationLoaded(load)