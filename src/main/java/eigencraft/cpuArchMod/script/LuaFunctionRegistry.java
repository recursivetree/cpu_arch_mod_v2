package eigencraft.cpuArchMod.script;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.HashMap;

public class LuaFunctionRegistry extends LuaObjectSetter{
    HashMap<RegistryKey,RegisterFunction> functionHashMap = new HashMap<>();
    LuaTable table = new LuaTable();

    public LuaFunctionRegistry(String name) {
        //Can't use before constructor
        super(name, null);
        this.object = table;
    }

    public class RegistryKey{
        private RegistryKey(){}
    }

    public LuaValue getFunction(RegistryKey key){
        return functionHashMap.get(key).getObject();
    }

    public RegistryKey register(String name){
        RegistryKey key = new RegistryKey();
        RegisterFunction f = new RegisterFunction();
        functionHashMap.put(key,f);
        table.set(LuaString.valueOf(name),f);
        return key;
    }

    private class RegisterFunction extends TwoArgFunction {
        public LuaValue getObject() {
            return object;
        }

        private LuaValue object = LuaValue.NIL;

        @Override
        public LuaValue call(LuaValue arg, LuaValue arg2) {
            System.out.println(arg2);
            //LuaValue k = LuaValue.NIL;
//            while ( true ) {
//                Varargs n = arg.next(k);
//                if ( (k = n.arg1()).isnil() )
//                    break;
//                LuaValue v = n.arg(2);
//                System.out.println(k);
//                System.out.println(v);
//            }
            object = arg2;
            return LuaValue.NIL;
        }
    }
}
