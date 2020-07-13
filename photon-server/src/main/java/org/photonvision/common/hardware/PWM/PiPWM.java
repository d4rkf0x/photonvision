/*
 * Copyright (C) 2020 Photon Vision.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.photonvision.common.hardware.PWM;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.exception.UnsupportedPinModeException;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

public class PiPWM extends PWMBase {

    static {
        // Initialize wiringPi Library
        Gpio.wiringPiSetup();
    }

    private int[] pwmRange = new int[2];
    private final int pin;

    public PiPWM(int pin, int value, int range) throws UnsupportedPinModeException {
        this.pin = pin;
        SoftPwm.softPwmCreate(pin, value, range);
    }

    @Override
    public void setPwmRange(int[] range) {
        SoftPwm.softPwmStop(pin);
        SoftPwm.softPwmCreate(pin, 0, range[1]);
        pwmRange = range;
    }

    @Override
    public int[] getPwmRange() {
        return pwmRange;
    }

    @Override
    public boolean shutdown() {
        SoftPwm.softPwmStop(pin);
        return true;
    }

    @Override
    public void dimLED(int dimPercentage) {
        // Check to see if dimPercentage is within the range
        if (dimPercentage < pwmRange[0] || dimPercentage > pwmRange[1]) return;
        SoftPwm.softPwmWrite(pin, dimPercentage);
    }
}
