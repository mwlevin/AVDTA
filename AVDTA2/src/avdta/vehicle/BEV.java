/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

/**
 *
 * @author ut
 */
public class BEV extends VehicleClass
{
    public String toString()
    {
        return "BEV";
    }
    
    private double rho = 1.2;						// density of air
    private double C_D = 0.32;						// drag area
    private double A = 2.5;						// frontal area
    private double C_RR = 0.01;						// rolling resistance coefficient
    private double m_total = 1904;					// mass of vehicle
    public static final double g = 9.81;	// acceleration from gravity
    private double k_m = 1.1; 						// rotational inertia
    private double eta_motor = 0.86;
    private double eta_battery = 0.95; 						// engine efficiency
    private double eta_trans = 0.87;				// transmission efficiency
    private double k_regen = 0.5;
    
    public double calcPower(double speed, double char_accel, double grad_angle)
    {
        double rmc_speed = speed;
        
        double P_accessory = 1000;

        speed /= 2.237;
        rmc_speed /= 2.237;
        char_accel /= 2.237;


        double P_aero = 0.5* rho * C_D * A * rmc_speed * rmc_speed * rmc_speed;
        double P_roll = C_RR*m_total * 9.81 * speed;
        double P_accel = k_m * m_total * char_accel * speed;
        double P_grade = m_total * g * Math.sin(grad_angle) * speed;
        double P_wheel = P_aero + P_roll + P_accel + P_grade;
        double P_inertia = m_total * k_m * char_accel * speed;

        double P_brake = (1 - k_regen) * Math.max(0, -P_wheel);
        double P_drive_loss = (1 - eta_trans * eta_motor) / eta_trans / eta_motor * (P_wheel + P_inertia) +
                (1 - eta_trans * eta_motor) * k_regen * P_inertia;
        double P_battery_loss = (1 - eta_battery) * Math.max(0, P_inertia) / eta_trans / eta_motor * (0.5 + 0.5* k_regen);
        double P_battery_power = P_wheel + P_brake + P_drive_loss + P_accessory + P_battery_loss;
        double P_recharge_loss = (1 - eta_battery) / eta_battery * Math.max(0, P_battery_power);
        double P_fuel = P_recharge_loss + P_battery_power;

        return P_fuel / 1000;
    }
}
