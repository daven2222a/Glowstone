package net.glowstone.inventory;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

/**
 * The complicated logic for determining how window click messages are
 * interpreted.
 */
public final class WindowClickLogic {

    private WindowClickLogic() {
    }

    public static ClickType getClickType(final int mode, final int button, final int slot) {
        // mode ; button ; slot (* = -999)
        // m b s
        // 0 0   lmb
        //   1   rmb
        // 1 0   shift+lmb
        //   1   shift+rmb (same as 1/0)
        // 2 *   number key b+1
        // 3 2   middle click / duplicate (creative only)
        // 4 0   drop
        // 4 1   ctrl + drop
        // 4 0 * lmb with no item (no-op)
        // 4 1 * rmb with no item (no-op)
        // 5 0 * start left drag
        // 5 1   add slot left drag
        // 5 2 * end left drag
        // 5 4 * start right drag
        // 5 5   add slot right drag
        // 5 6 * end right drag
        // 6 0   double click
        switch (mode) {
            case 0: // normal click
                if (button == 0) {
                    return slot == -1 ? ClickType.WINDOW_BORDER_LEFT : ClickType.LEFT;
                } else if (button == 1) {
                    return slot == -1 ? ClickType.WINDOW_BORDER_RIGHT : ClickType.RIGHT;
                }
                break;

            case 1: // shift click
                if (button == 0) {
                    return ClickType.SHIFT_LEFT;
                } else if (button == 1) {
                    return ClickType.SHIFT_RIGHT;
                }
                break;

            case 2: // number key
                return ClickType.NUMBER_KEY;

            case 3: // middle click
                if (button == 2) {
                    return ClickType.MIDDLE;
                }
                break;

            case 4: // drop or ctrl+drop
                if (button == 0) {
                    return ClickType.DROP;
                } else if (button == 1) {
                    return ClickType.CONTROL_DROP;
                }
                break;

            case 5: // drag
                break;

            case 6:
                return ClickType.DOUBLE_CLICK;
        }
        return null;
    }

    public static InventoryAction getAction(ClickType clickType, int slot, ItemStack cursor, ItemStack slotItem) {
        switch (clickType) {
            case LEFT:
                // "SWAP_WITH_CURSOR", "PLACE_ONE", "DROP_ALL_CURSOR", "PLACE_ALL", "PLACE_SOME", "NOTHING", "PICKUP_ALL"

                if (cursor == null) {
                    if (slot < 0 || slotItem == null) {
                        return InventoryAction.NOTHING;
                    }
                    return InventoryAction.PICKUP_ALL;
                }

                if (slot < 0) {
                    return InventoryAction.DROP_ALL_CURSOR;
                }

                if (slotItem == null) {
                    return InventoryAction.PLACE_ALL;
                }

                if (slotItem.isSimilar(cursor)) {
                    int transfer = Math.min(cursor.getAmount(), slotItem.getType().getMaxStackSize() - slotItem.getAmount());
                    if (transfer == 0) {
                        return InventoryAction.NOTHING;
                    } else if (transfer == 1) {
                        return InventoryAction.PLACE_ONE;
                    } else if (transfer == cursor.getAmount()) {
                        return InventoryAction.PLACE_ALL;
                    } else {
                        return InventoryAction.PLACE_SOME;
                    }
                }

                return InventoryAction.SWAP_WITH_CURSOR;

            case RIGHT:
                // "NOTHING", "PLACE_ONE", "PICKUP_HALF", "DROP_ONE_CURSOR", "SWAP_WITH_CURSOR"
                if (cursor == null) {
                    if (slot < 0 || slotItem == null) {
                        return InventoryAction.NOTHING;
                    }
                    return InventoryAction.PICKUP_HALF;
                }

                if (slot < 0) {
                    return InventoryAction.DROP_ONE_CURSOR;
                }

                if (slotItem == null) {
                    return InventoryAction.PLACE_ONE;
                }

                if (cursor.isSimilar(slotItem)) {
                    if (slotItem.getAmount() + 1 <= slotItem.getType().getMaxStackSize()) {
                        return InventoryAction.PLACE_ONE;
                    }
                    return InventoryAction.NOTHING;
                }

                return InventoryAction.SWAP_WITH_CURSOR;

            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                if (slotItem != null) {
                    return InventoryAction.MOVE_TO_OTHER_INVENTORY;
                } else {
                    return InventoryAction.NOTHING;
                }

            case WINDOW_BORDER_LEFT:
            case WINDOW_BORDER_RIGHT:
                return InventoryAction.NOTHING;

            case MIDDLE:
                throw new UnsupportedOperationException("MIDDLE not supported yet");

            case NUMBER_KEY:
                // {"NUMBER_KEY", "NOTHING", "HOTBAR_SWAP"},
                // note: should treat as NOTHING if there is no item in the hotbar
                return InventoryAction.HOTBAR_SWAP;

            case DOUBLE_CLICK:
                if (cursor != null) {
                    return InventoryAction.COLLECT_TO_CURSOR;
                } else {
                    return InventoryAction.NOTHING;
                }

            case DROP:
                // {"DROP", "DROP_ONE_SLOT"},
                return InventoryAction.DROP_ONE_SLOT;

            case CONTROL_DROP:
                return InventoryAction.DROP_ALL_SLOT;

            case CREATIVE:
                throw new UnsupportedOperationException("CREATIVE not supported yet");

            case UNKNOWN:
            default:
                return InventoryAction.UNKNOWN;
        }
    }

    public static boolean isPlaceAction(InventoryAction action) {
        switch (action) {
            case SWAP_WITH_CURSOR:
            case PLACE_ONE:
            case PLACE_ALL:
            case PLACE_SOME:
                return true;
        }
        return false;
    }
}