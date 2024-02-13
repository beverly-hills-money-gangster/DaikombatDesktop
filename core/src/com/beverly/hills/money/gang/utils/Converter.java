package com.beverly.hills.money.gang.utils;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.proto.ServerResponse;

public interface Converter {


    static Vector2 convertToVector2(ServerResponse.Vector vector) {
        return new Vector2(vector.getX(), vector.getY());
    }
}
