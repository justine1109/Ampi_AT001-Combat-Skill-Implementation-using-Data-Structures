package com.mycompany.ampicombatskill;

import java.util.Scanner;
import java.util.Random;
import java.util.HashMap;
import java.util.Stack;

    public class AmpiUndanganDaelCombatSkill {
        
    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();
    
         // Player stats
        static class Player {
        int maxHP = 1200;
        int hp = 1200;
        int atk = 150;
        int def = 80;
        int jinguStacks = 0;
        final int maxJinguStacks = 4;
        boolean jinguActive = false;
        //int jinguStackDuration = 0;
        int jinguCountervar = 0;
        Stack <Integer> jingustackduration = new Stack<>();
        

        boolean alive = true;

        // Calculate attack damage including Jingu Mastery
        int attackDamage() {
            if (!alive) return 0;
            int damage = atk;
            jinguCountervar += 1;
            jingustackduration.push(jinguCountervar);
            if (jinguStacks < maxJinguStacks) {
                jinguStacks++;
                System.out.println("Jingu Mastery stack gained! (" + jinguStacks + "/" + maxJinguStacks + ")");
            } else if (jinguStacks == maxJinguStacks) {
                jinguActive = true;
                //jinguCounter = 0;
                System.out.println("Jingu Mastery Activated! Next hits deal bonus damage and lifesteal.");
            }
            if (jinguActive) {
                int bonus = 40;
                //jinguCounter++;
                System.out.println("Jingu Mastery hit! Bonus damage +" + bonus);
                damage += bonus;
            }
            return damage;
        }

        // Calculate counter damage scaled on DEF with enhancement when hp <= 50%
        int counterDamage() {
            if (!alive) return 0;
            int baseDamage = def * 2;
            if (hp <= maxHP / 2) {
                baseDamage = (int)(baseDamage * 1.6);
            }
            // Include Jingu Mastery bonus if active
            if (jinguActive) {
                baseDamage += baseDamage * 40;
            }
            return baseDamage;
        }

        void heal(int amount) {
            if (!alive) return;
            hp = Math.min(hp + amount, maxHP);
            System.out.println("Player heals " + amount + " HP from Jingu Mastery lifesteal.");
        }

        void takeDamage(int damage) {
            if (!alive) return;
            hp -= damage;
            if (hp < 0) hp = 0;
            System.out.println("Player takes " + damage + " damage.");
            if (hp == 0) {
                alive = false;
                System.out.println("Player is defeated! Game Over.");
            }
        }
    }

    // Enemy stats
    static class Enemy {
        int maxHP = 1200;
        int hp = 1200;
        Stack<Integer> hpStack = new Stack<>();
        HashMap<String, Object> statuses = new HashMap<>(); // For poison stacks and timers
        boolean alive = true;

        // For simplicity, poison status structure: "poisonStacks" -> Integer, "poisonEndTime" -> Long (millis)
        void init() {
            hpStack.push(maxHP);
            statuses.put("poisonStacks", 0);
            statuses.put("poisonEndTime", 0L);
        }

        int getPoisonStacks() {
            return (int)statuses.get("poisonStacks");
        }

        void setPoisonStacks(int val) {
            statuses.put("poisonStacks", val);
        }

        long getPoisonEndTime() {
            return (long)statuses.get("poisonEndTime");
        }

        void setPoisonEndTime(long time) {
            statuses.put("poisonEndTime", time);
        }

        boolean isPoisonActive() {
            long now = System.currentTimeMillis();
            return getPoisonStacks() > 0 && now < getPoisonEndTime();
        }

        // Enemy takes damage or heals if poisoned per Toxic Rejuvenation passive
        void takeDamage(int damage) {
            if (!alive) return;

            if (isPoisonActive()) {
                // Heal instead of taking damage
                int newHp = hp + damage;
                if (newHp > maxHP) newHp = maxHP;
                boolean reverted = updateHpStack(newHp);
                if (!reverted) {
                    hp = newHp;
                    System.out.println("Enemy is poisoned and heals " + damage + " HP instead of taking damage.");
                }
            } else {
                // Take damage normally
                int newHp = hp - damage;
                if (newHp < 0) newHp = 0;
                boolean reverted = updateHpStack(newHp);
                if (!reverted) {
                    hp = newHp;
                    System.out.println("Enemy takes " + damage + " damage.");
                }
                if (hp == 0) {
                    alive = false;
                    System.out.println("Enemy is defeated! You win!");
                }
            }
        }

        // Update HP stack with 25% chance to revert to previous state instead of applying newHP
        boolean updateHpStack(int newHp) {
            // Push newHp to stack
            hpStack.push(newHp);
            // Check for revert - only if stack length > 1
            if (hpStack.size() > 1) {
                double chance = random.nextDouble();
                if (chance < 0.25) {
                    // revert to previous
                    hpStack.pop();
                    hp = hpStack.peek();
                    System.out.println("Enemy reverted to previous HP stack: " + hp);
                    return true; // reverted
                }
            }
            return false; // not reverted, new hp applied
        }

        // Apply poison stack, max 3 stacks, refreshes duration 13 seconds
        void applyPoison() {
            int stacks = getPoisonStacks();
            if (stacks < 3) {
                stacks++;
                setPoisonStacks(stacks);
                System.out.println("Enemy poisoned! Stacks: " + stacks);
            } else {
                System.out.println("Enemy poison stack maxed at 3.");
            }
            setPoisonEndTime(System.currentTimeMillis() + 13000L); // 13 seconds duration
        }

        // Enemy attack player
        void attack(Player player) {
            if (!alive) return;
            int baseDamage = 140;
            int variance = random.nextInt(21) - 10; // -10 to +10
            int damage = baseDamage + variance;
            if (damage < 0) damage = 0;
            // Player damage reduced by half of player's def
            int damageToPlayer = damage - (player.def / 2);
            if (damageToPlayer < 0) damageToPlayer = 0;
            System.out.println("Enemy attacks and deals " + damageToPlayer + " damage to Player.");
            player.takeDamage(damageToPlayer);
        }
    }

    public static void main(String[] args) {
        Player player = new Player();
        Enemy enemy = new Enemy();
        enemy.init();

        System.out.println("Turn-Based Game Started: Player VS Enemy");
        System.out.println("Player HP: " + player.hp + "/" + player.maxHP + " | Enemy HP: " + enemy.hp + "/" + enemy.maxHP);

        boolean playerTurn = true;

        while (player.alive && enemy.alive) {
            if (playerTurn) {
                System.out.println("\nYour turn! Choose an action:");
                System.out.println("1. Attack");
                System.out.println("2. Use Skill (Counter)");
                System.out.print("Enter choice: ");
                String input = scanner.nextLine();

                if (input.equals("1")) {
                    int dmg = player.attackDamage();
                    enemy.takeDamage(dmg);

                    if (player.jinguActive) {
                        player.heal(12); // lifesteal ~30% of 40 bonus damage
                        player.jinguStacks = 0;
                        player.jinguCountervar = 0;
                        player.jingustackduration.push(0);
                        player.jinguActive = false;
                        int jinguCountervar = 0;
                                
                    }
                } else if (input.equals("2")) {
                    int dmg = player.counterDamage();

                    // 30% chance to poison enemy
                    if (random.nextDouble() < 0.3) {
                        enemy.applyPoison();
                    }
                    System.out.println("Player uses Counter skill dealing " + dmg + " damage!");
                    enemy.takeDamage(dmg);

                    // Reset Jingu stacks/counter on skill use
                    player.jinguStacks = 0;
                    player.jinguActive = false;
                    //player.jinguCounter = 0;
                } else {
                    System.out.println("Invalid choice. Try again.");
                    continue; // retry current turn
                }

                System.out.printf("Player HP: %d/%d | Enemy HP: %d/%d\n", player.hp, player.maxHP, enemy.hp, enemy.maxHP);
                if (!enemy.alive) break;

                playerTurn = false; // switch to enemy turn
            } else {
                System.out.println("\nEnemy's turn...");
                enemy.attack(player);

                System.out.printf("Player HP: %d/%d | Enemy HP: %d/%d\n", player.hp, player.maxHP, enemy.hp, enemy.maxHP);
                if (!player.alive) break;

                playerTurn = true; // switch to player turn
            }
        }

        System.out.println("\nGame Over.");
        if (player.alive) {
            System.out.println("Congratulations! You won!");
        } else {
            System.out.println("You lost! Try again.");
        }
    }
}
